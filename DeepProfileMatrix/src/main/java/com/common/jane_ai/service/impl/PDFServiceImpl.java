package com.common.jane_ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.entity.PDFentity;
import com.common.jane_ai.Obj.vo.PDFVO;

import com.common.jane_ai.caffine.PDFlocalCache;
import com.common.jane_ai.caffine.RankingLocalCache;
import com.common.jane_ai.constant.JaneRedisKeyConstant;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import com.common.jane_ai.mapper.PDFMapper;
import com.common.jane_ai.service.PDFService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.common.jane_ai.constant.JaneDescConstant.JANE_PDF_DESC;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_PDF_FILE;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Service
@Slf4j
public class PDFServiceImpl implements PDFService {

    // 每次分析简历时获取 prototype 客户端，避免一直使用服务启动时的默认模型。
    @Autowired
    @Qualifier("RAG_Pinecone_chatClient")
    private ObjectFactory<ChatClient> ragPineconeChatClientFactory;

    @Autowired
    private PDFlocalCache pdflocalCache;
    @Autowired
    private RankingLocalCache rankingLocalCache;
    @Autowired
    private PDFMapper pdfMapper;

    // 🌟 1. 注入 VectorStore (Spring AI 会自动给你配好的 PineconeVectorStore)
    @Autowired
    private VectorStore vectorStore;

    @Override
    /**
     * 解析处理PDF文件
     */
    public Flux<String> uploadPDF(MultipartFile file, String chatId) {
        // 内部调用，chatClient，进行简单的摘要分析并返回数据
        // 可以指定functioncalling去获取到文档内容进行打分//更新排行榜
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        String fileKey = JANE_PDF_FILE + userId;
        // 1. 获取 PDF 纯文本
        String text = getByPDF(file);//这里可以存储，
        // 2. 放在本地缓存 Caffine
        pdflocalCache.putpdfFile(fileKey, text);
        log.info("PDF文件已解析并存储上下文-{}", userId);

        // 3. 暂停简历向量化入库；保留原逻辑，恢复向量服务后取消下方注释即可重新启用。
        // try {
        //     storeToPinecone(text, userId);
        //     log.info("✅ PDF文本已成功切块、向量化并存入 Pinecone, userId: {}", userId);
        // } catch (Exception e) {
        //     log.error("❌ 存入 Pinecone 失败: {}", e.getMessage(), e);
        //     // 向量入库失败仅记录日志，继续简历分析。
        // }
        // 4. 调用 chatid 对应 chatclient 进行会话解析并返回流
        return PDFchat(chatId, text);

    }

    @Override
    public List<PDFVO> ranking() {
        //定时任务，放在Caffine中.
        if (!rankingLocalCache.getRanking(JaneRedisKeyConstant.JANE_RANKING).isEmpty()) {
            return rankingLocalCache.getRanking(JaneRedisKeyConstant.JANE_RANKING);
        }
        LambdaQueryWrapper<PDFentity> wrapper = Wrappers.lambdaQuery(PDFentity.class)
                .eq(PDFentity::getStatus, true)
                .orderByDesc(PDFentity::getPdfScore)
                .last("limit 20");
        List<PDFentity> pdfentityList = null;
        try {
            pdfentityList = pdfMapper.selectList(wrapper);
        } catch (Exception e) {
            throw new JaneFrameworkException(BaseCode.RANKING_ERROR);
        }
        List<PDFVO> pdfvos = pdfentityList.stream().map(PDFentity -> new PDFVO(PDFentity.getPdfScore(), PDFentity.getUserName(), PDFentity.getPdfUpdateCount(), PDFentity.getUpdateTime()))
                .toList();
        rankingLocalCache.putRanking(JaneRedisKeyConstant.JANE_RANKING, pdfvos);
        return pdfvos;
    }

    /**
     * PDF解析
     *
     * @param file
     * @return
     */
    public String getByPDF(MultipartFile file) {
        //Spring 对上传文件的封装，内部帮你处理了表单中文件的解析工作，你可以像操作普通对象一样访问文件名、内容、大小等。
        //try with ressource自动释放资源
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new JaneFrameworkException(BaseCode.PDF_UPLOAD_ERROR);
        }
    }

    /**
     * 解析用户简历生成摘要
     *
     * @param chatId
     * @param text
     */
    public Flux<String> PDFchat(String chatId, String text) {
        String currentUserId = BaseParameterHolder.getBaseParameter(USER_ID);
        // 同时创建当前模型对应的对话客户端和标题客户端，并保留工具调用能力。
        ChatClient currentChatClient = ragPineconeChatClientFactory.getObject();
        return currentChatClient.prompt() // 调用chatClient的prompt方法开始构建用户提示
                .user(JANE_PDF_FILE + text)
                .system(JANE_PDF_DESC)
                .toolContext(Map.of(USER_ID, currentUserId == null ? "0" : currentUserId))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(USER_ID, BaseParameterHolder.getBaseParameter(USER_ID))
                ).stream()
                .content();
    }

    /**
     * 负责将长文本切片并写入 Pinecone；当前调用入口已注释，保留实现供后续恢复。
     */
    private void storeToPinecone(String text, String userId) {
        // 1. 构建元数据 Metadata (必须用可变的 Map，方便后续动态添加)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);         // 将 userId 作为关键过滤条件
        metadata.put("source", "user_resume");  // 标记文件来源

        // 2. 将整个长文本包装成一个原始的 Spring AI Document
        Document originalDoc = new Document(text, metadata);

        // 3. 配置文本分割器 (TokenTextSplitter)
        TokenTextSplitter textSplitter = new TokenTextSplitter(
                800,  // chunkSize: 每块包含的 token 数量
                100,  // minChunkSizeChars
                100,  // minChunkLengthToEmbed
                200,  // maxNumChunks
                true  // keepSeparator
        );

        // 4. 执行切片，切出来的每个片段都会自动带上上面配置的 userId 等 Metadata
        List<Document> chunkedDocs = textSplitter.apply(List.of(originalDoc));

        // 5. 分批写入向量库 (防止 PDF 太长导致一次性请求过大，报 400 错误)
        int batchSize = 10;
        for (int i = 0; i < chunkedDocs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunkedDocs.size());
            List<Document> batch = chunkedDocs.subList(i, end);

            // 这一步底层的 PineconeVectorStore 会自动调用 EmbeddingModel 将文本转为向量并上传
            vectorStore.add(batch);
        }
    }
}
