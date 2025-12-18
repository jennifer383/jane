package com.common.jane_ai.controller;

import com.common.jane_ai.RepateExecuteLimit.RepateExecuteLimit;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.ModelService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

import static com.common.jane_ai.constant.JaneTypeConstant.AUTHORIZATION;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;
@Slf4j
@RestController
@RequestMapping("/jane")
public class JaneController {

    @Autowired
    @Qualifier("chatClient")
    private ObjectFactory<ChatClient> chatClientFactory;

    @Autowired
    @Qualifier("RAG_Pinecone_chatClient")
    private ObjectFactory<ChatClient> rag_Pinecone_chatClientFactory;
    @Value("${spring.ai.ollama.chat.model:}")
    private String ollamaModel;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String openaiModel;

    @Value("${spring.ai.deepseek.chat.options.model:}")
    private String deepseekModel;

    //.stream() ✅ 触发实际的 AI 请求，并以流式响应的方式返回结果。这是真正向 AI 模型发送请求的方法调用。
    //.content() 从流式响应中提取文本内容返回给客户端
    @RepateExecuteLimit(type = JaneTypeConstant.CHAT_MIDENG, chatId = "#chatId", durationTime = 5L)
    @PostMapping(value="/chat",produces = "text/plain;charset=utf-8")//返回文本流格式响应
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId){
        log.info("REQUEST_CONTROLLER-"+Thread.currentThread().getName());
        try {
            // 每次请求时动态获取最新的ChatClient实例，确保使用当前选择的模型
            // 使用普通的chatClient，不依赖Pinecone向量数据库，避免Stream processing failed
            ChatClient currentChatClient = chatClientFactory.getObject();
            // 该方法通过一系列链式调用来构建和发送用户提示，并获取响应内容
            // 使用流式调用，逐块返回响应内容
            return currentChatClient.prompt() // 调用chatClient的prompt方法开始构建用户提示
                    .user(prompt) // 设置用户提示的内容为prompt
                    //你配置一次，多个 advisor 会“监听”自己关心的参数，然后各自执行自己的逻辑。
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId)
                            .param(USER_ID, BaseParameterHolder.getBaseParameter(USER_ID))
                    )//ChatClient 配置逻辑本身没有问题， 但由于在 RedisChatHistoryAdvisor 里使用了
                    // BaseParameterHolder依赖 ThreadLocal）而 ChatClient 是基于 Reactor 的异步响应式执行模型，
                    // 导致你当前上下文无法传递进 Advisor，从而抛出 NullPointerException。
                //给请求的“增强器”传入一个参数
                    .stream() // 流式调用
                    .content(); // 从响应对象中提取内容流
        } catch (Exception e) {
            log.error("Chat request failed: {}", e.getMessage(), e);
            // 将错误信息作为流返回
            return Flux.just("Error: " + e.getMessage());
        }
    }
    @Autowired
    private ModelService modelService;

    @GetMapping("/model")
    public ApiResponse<Map<String, String>> getModel() {
        Map<String, String> info = new HashMap<>();
        // 从 ModelService 获取当前模型信息
        String currentModel = modelService.getCurrentModel();
        info.put("model", currentModel);

        // 根据当前模型名称确定提供者
        String provider;
        if (currentModel.equals("qwen-turbo")) {
            provider = "openai";
        } else if (currentModel.equals("deepseek-chat")) {
            provider = "deepseek";
        } else {
            // 所有Ollama模型
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


}
