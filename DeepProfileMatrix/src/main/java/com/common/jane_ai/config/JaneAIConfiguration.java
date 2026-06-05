package com.common.jane_ai.config;

import com.common.jane_ai.advisor.RedisChatHistoryAdvisor;
import com.common.jane_ai.advisor.RedisChatHistoryTitleAdvisor;
import com.common.jane_ai.functioncalling.JaneMarkController;
import com.common.jane_ai.redis_chat_memory.RedisChatMemoryRepository;
import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import com.common.jane_ai.service.ModelService;
import com.common.jane_ai.service.RedisChatHistoryAdvisorService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static com.common.jane_ai.constant.JaneDescConstant.*;

//@Configuration + @Bean 是标准且推荐的方式向 Spring IOC 容器中注入对象。
//不需要配置 META-INF 下的 spring.factories / spring.components，除非你在开发“自动装配的库”
@Configuration
public class JaneAIConfiguration{

    @Value("${spring.ai.vectorstore.pinecone.api-key}")
    private String pineconeApiKey;
    @Value("${spring.ai.vectorstore.pinecone.index-name}")
    private String pineconeIndexName;

    @Autowired
    private RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;

    @Bean                               //参数在容器中自动获取，无需显式注入
    public ChatMemoryRepository chatMemoryRepository(RedisChatMemoryRepositoryDialect dialect) {
        return new RedisChatMemoryRepository(dialect);
    }




    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(30)
                .build();

    }

    //前端生成的 chatId 是使用 JavaScript 的 Date.now() 毫秒级时间戳，
    // 并转为字符串，也就是“13 位的 Unix 毫秒时间戳字符串”（例如：1712345678901）



    // 注意：保留配置在 application.yml；Spring AI 的自动配置会创建 `vectorStore` bean。
    // 为避免与自动配置产生 bean 名称冲突，这里不再手动定义 VectorStore bean。

    @Bean(name = "titleChatClient")// 生成Title
    @Scope("prototype") // 设置为prototype，每次请求时创建新实例
    public ChatClient titleChatClient(
            OllamaChatModel ollamaChatModel,
            OpenAiChatModel openAiChatModel,
            DeepSeekChatModel deepSeekChatModel,
            ModelService modelService) {
        // 根据当前选择的模型动态创建titleChatClient
        String currentModel = modelService.getCurrentModel();

        // 根据当前模型选择对应的ChatModel
        ChatModel chatModel;
        if (currentModel.equals("qwen-turbo") || currentModel.equals("kimi-k2-thinking")) {
            chatModel = openAiChatModel;
        } else if (currentModel.equals("deepseek-chat")) {
            chatModel = deepSeekChatModel;
        } else {
            chatModel = ollamaChatModel;
        }

        return ChatClient
                .builder(chatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean(name = "chatClient")//如果 Bean A 需要 Bean B 作为构造参数或 @Autowired 依赖，Spring 会先创建 Bean B，再创建 Bean A。
    @Scope("prototype")
    public ChatClient chatClient(
            OllamaChatModel ollamaChatModel,
            OpenAiChatModel openAiChatModel,
            DeepSeekChatModel deepSeekChatModel,
            ChatMemory chatMemory,
            @Qualifier("titleChatClient") ObjectFactory<ChatClient> titleChatClientFactory,
            ModelService modelService) {
        // 根据当前选择的模型动态创建ChatClient
        String currentModel = modelService.getCurrentModel();

        // 根据当前模型选择对应的ChatModel
        ChatModel chatModel;
        if (currentModel.equals("qwen-turbo") || currentModel.equals("kimi-k2-thinking")) {
            chatModel = openAiChatModel;
        } else if (currentModel.equals("deepseek-chat")) {
            chatModel = deepSeekChatModel;
        } else {
            chatModel = ollamaChatModel;
        }

        // 每次创建chatClient时，从工厂获取最新的titleChatClient实例
        ChatClient titleChatClient = titleChatClientFactory.getObject();

        return ChatClient.builder(chatModel)
                .defaultSystem(JANE_DESC)//系统描述
                .defaultAdvisors(
                        // chat请求的拦截器增强器
                        new SimpleLoggerAdvisor(),//DEBUG日志记录器
                        RedisChatHistoryAdvisor.builder(redisChatHistoryAdvisorService)
                                .order(JANE_CHAT_HISTORY_ADVISOR_ORDER).build(),
                        RedisChatHistoryTitleAdvisor.builder(redisChatHistoryAdvisorService)
                                .order(JANE_CHAT_TITLE_ADVISOR_ORDER)
                                .chatMemory(chatMemory).chatClient(titleChatClient)
                                .build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(JANE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .build();
    }

    @Bean(name = "RAG_Pinecone_chatClient")
    @Scope("prototype")
//   RAG Client
    public ChatClient RAGchatClient(
             OllamaChatModel ollamaChatModel,
             OpenAiChatModel openAiChatModel,
             DeepSeekChatModel deepSeekChatModel,
             ChatMemory chatMemory,
             @Qualifier("titleChatClient") ObjectFactory<ChatClient> titleChatClientFactory,
                                     VectorStore vectorStore,
                                     JaneMarkController janeMarkController,
                                       ModelService modelService) {

        // 根据当前选择的模型动态创建ChatClient
        String currentModel = modelService.getCurrentModel();

        // 根据当前模型选择对应的ChatModel
        ChatModel chatModel;
        if (currentModel.equals("qwen-turbo") || currentModel.equals("kimi-k2-thinking")) {
            chatModel = openAiChatModel;
        } else if (currentModel.equals("deepseek-chat")) {
            chatModel = deepSeekChatModel;
        } else {
            chatModel = ollamaChatModel;
        }

        // 每次创建RAGchatClient时，从工厂获取最新的titleChatClient实例
        ChatClient titleChatClient = titleChatClientFactory.getObject();

        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(JANE_DESC)//系统描述
                .defaultAdvisors(
                        // chat请求的拦截器增强器
                        new SimpleLoggerAdvisor(),//DEBUG日志记录器
                        RedisChatHistoryAdvisor.builder(redisChatHistoryAdvisorService)
                                .order(JANE_CHAT_HISTORY_ADVISOR_ORDER).build(),
                        RedisChatHistoryTitleAdvisor.builder(redisChatHistoryAdvisorService)
                                .order(JANE_CHAT_TITLE_ADVISOR_ORDER)
                                .chatMemory(chatMemory).chatClient(titleChatClient)
                                .build(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .order(JANE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.7)//相似度赋值
                                        .topK(8)// 返回的相似文档数量
                                        .build())
                                .build()
                        //ChatClient 会自动从向量数据库中检索相关文档，并将这些文档作为上下文拼接进用户提问的 Prompt 中（也就是放进 Q）去参与问答生成
                );

        // 添加工具，仅当janeMarkController不为null时
        if (janeMarkController != null) {
            builder = builder.defaultTools(janeMarkController);
        }

        return builder.build();
        // 相似度阈值
        //作用：设置余弦相似度的下限值。
        //含义：只有相似度 ≥ 0.7 的文档才会被返回。
        //默认值：通常是 0.0（即不限制），你设置为 0.7 说明你只要“更相关”的文档。
        //范围：0.0 ~ 1.0，1.0 表示完全一致。
    }
}
