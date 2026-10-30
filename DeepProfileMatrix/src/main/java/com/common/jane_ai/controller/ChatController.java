package com.common.jane_ai.controller;

import com.common.jane_ai.RepateExecuteLimit.RepateExecuteLimit;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.AsyncService;
import com.common.jane_ai.service.ModelService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@RestController
@RequestMapping("/api")
@Validated
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    @Autowired
    @Qualifier("chatClient")
    private ObjectFactory<ChatClient> chatClientFactory;

    @Value("${spring.ai.ollama.chat.model:}")
    private String ollamaModel;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String openaiModel;

    @Value("${spring.ai.deepseek.chat.options.model:}")
    private String deepseekModel;

    @Autowired
    @Qualifier("RAG_Pinecone_chatClient")
    private ObjectFactory<ChatClient> ragPineconeChatClientFactory;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ModelService modelService;



    //.stream() ✅ 触发实际的 AI 请求，并以流式响应的方式返回结果。这是真正向 AI 模型发送请求的方法调用。
    //.content() 从流式响应中提取文本内容返回给客户端
    @RepateExecuteLimit(type = JaneTypeConstant.CHAT_MIDENG, chatId = "#chatId", durationTime = 5L)
    @PostMapping(value="/chat",produces = "text/html;charset=utf-8")//浏览器会收到带 Content-Type: text/html;charset=utf-8 的响应，显示网页内容，不会乱码
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId){
        log.info("REQUEST_CONTROLLER-"+Thread.currentThread().getName());


        // 按本次请求时选择的模型创建客户端，避免复用启动时注入的旧客户端。
        ChatClient currentChatClient = chatClientFactory.getObject();
        return currentChatClient.prompt() // 调用chatClient的prompt方法开始构建用户提示
                .user(prompt) // 设置用户提示的内容为prompt
                //你配置一次，多个 advisor 会“监听”自己关心的参数，然后各自执行自己的逻辑。
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId)
                        .param(USER_ID, BaseParameterHolder.getBaseParameter(USER_ID))
                )//ChatClient 配置逻辑本身没有问题， 但由于在 RedisChatHistoryAdvisor 里使用了
                // BaseParameterHolder依赖 ThreadLocal）而 ChatClient 是基于 Reactor 的异步响应式执行模型，
                // 导致你当前上下文无法传递进 Advisor，从而抛出 NullPointerException。
                //给请求的“增强器”传入一个参数
                .stream() // 流式返回
                .content(); // 从响应对象中提取内容并返回

//        ChatMemoryAdvisor 看到有 conversation_id，它就参与处理
//        AuthAdvisor 看到有 auth_token，它就去校验权限
//        LoggingAdvisor 不关心这些参数，它就跳过了



    }

    @GetMapping("/model")
    public ApiResponse<Map<String, String>> getModel() {
        Map<String, String> info = new HashMap<>();
        // 从 ModelService 获取当前模型信息
        String currentModel = modelService.getCurrentModel();
        info.put("model", currentModel);

        // 根据当前模型名称确定提供者
        String provider;
        if (currentModel.equals("gpt-4-turbo") || currentModel.equals("kimi-k2-thinking")) {
            provider = "openai";
        } else if (currentModel.equals("deepseek-chat")) {
            provider = "deepseek";
        } else {
            provider = "ollama";
        }
        info.put("provider", provider);
        return ApiResponse.ok(info);
    }

    @PostMapping("/model")
    public ApiResponse<Map<String, String>> changeModel(@RequestParam String model) {
        boolean success = modelService.changeModel(model);
        if (success) {
            Map<String, String> info = new HashMap<>();
            info.put("model", model);
            info.put("status", "success");
            return ApiResponse.ok(info);
        } else {
            return ApiResponse.error("模型切换失败");
        }
    }

    @PostMapping(value="/chat/upload", consumes = "multipart/form-data", produces = "text/html;charset=utf-8")
    public Flux<String> chatWithUpload(@RequestParam("prompt") @Size(min = 1, max = 1000, message = "prompt长度必须在1-1000之间") String prompt,
                                       @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                       @RequestHeader @NotBlank(message = "chatId不能为空") String chatId,
                                       @RequestHeader @NotBlank(message = "userId不能为空") String userId) {

        // 只使用过滤器验证过的身份，并在解析文件、切换线程之前保存。
        String currentUserId = BaseParameterHolder.getBaseParameter(USER_ID);
        Map<String, Object> toolContext = Map.of(USER_ID, currentUserId == null ? "0" : currentUserId);

        // 如果没有文件，直接返回普通聊天响应
        if (files == null || files.isEmpty()) {
            return chatClientFactory.getObject().prompt().user(prompt).toolContext(toolContext).stream().content();
        }

        // 在异步解析文件前获取当前客户端，确保本次上传使用请求开始时选择的模型。
        ChatClient currentChatClient = ragPineconeChatClientFactory.getObject();
        // 使用异步方式处理所有PDF文件
        return Mono.fromFuture(() -> {
            // 异步提取所有PDF文件内容
            List<java.util.concurrent.CompletableFuture<String>> futures = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(asyncService::extractPDFTextAsync)
                    .toList();

            // 等待所有异步任务完成
            java.util.concurrent.CompletableFuture<Void> allOf = java.util.concurrent.CompletableFuture.allOf(
                    futures.toArray(new java.util.concurrent.CompletableFuture[0])
            );

            // 收集所有结果
            return allOf.thenApply(v -> {
                StringBuilder ctx = new StringBuilder();
                for (int i = 0; i < futures.size(); i++) {
                    String extracted = futures.get(i).join();
                    if (extracted != null && !extracted.isEmpty()) {
                        MultipartFile file = files.get(i);
                        ctx.append("\n[文件:")
                          .append(file.getOriginalFilename())
                          .append("]\n")
                          .append(extracted);
                    }
                }
                return ctx;
            });
        }).flatMapMany(ctx -> {
            // 根据是否有提取内容选择不同的聊天客户端
            if (ctx.length() > 0) {
                return currentChatClient.prompt()
                        .user(prompt + "\n\n附加上下文（来自上传文件）:\n" + ctx)
                        .toolContext(toolContext)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId)
                                .param(USER_ID, currentUserId))
                        .stream()
                        .content();
            }
            return currentChatClient.prompt().user(prompt).toolContext(toolContext).stream().content();
        });
    }

    private String extractText(MultipartFile file) {
        try {
            // 验证文件类型
            if (!file.getContentType().equals("application/pdf")) {
                return "";
            }
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            log.error("PDF文件提取失败: {}", e.getMessage());
            return "";
        }
    }

}
