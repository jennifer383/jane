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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static com.common.jane_ai.constant.JaneDescConstant.JANE_PDF_DESC;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_PDF_FILE;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Service
@Slf4j
public class PDFServiceImpl implements PDFService {

    @Autowired
    @Qualifier("chatClient")
    private ChatClient chatClient;

    @Autowired
    @Qualifier("RAG_Pinecone_chatClient")
    private ChatClient rag_Pinecone_chatClient;

    @Autowired
    private PDFlocalCache pdflocalCache;
    @Autowired
    private RankingLocalCache rankingLocalCache;
    @Autowired
    private PDFMapper pdfMapper;

    @Override
    /**
     * 解析处理PDF文件
     */
    public Flux<String> uploadPDF(MultipartFile file, String chatId) {
        // 内部调用，chatClient，进行简单的摘要分析并返回数据
        // 可以指定functioncalling去获取到文档内容进行打分//更新排行榜
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        String fileKey = JANE_PDF_FILE + userId;
        String text = getByPDF(file);//这里可以存储，
        pdflocalCache.putpdfFile(fileKey,text);//放在本地缓存Caffine
        log.info("PDF文件已解析并存储上下文-{}",userId);
        //调用chatid对应chatclient进行会话解析
        return PDFchat(chatId, text);

    }

    @Override
    public List<PDFVO> ranking() {
        //定时任务，放在Caffine中.
        if(!rankingLocalCache.getRanking(JaneRedisKeyConstant.JANE_RANKING).isEmpty()){
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
     * @param file
     * @return
     */
    public String getByPDF(MultipartFile file){
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
     * @param chatId
     * @param text
     */
    public Flux<String> PDFchat(String chatId,String text){
        return rag_Pinecone_chatClient.prompt() // 调用chatClient的prompt方法开始构建用户提示
                .user(JANE_PDF_FILE+text)
                .system(JANE_PDF_DESC)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(USER_ID, BaseParameterHolder.getBaseParameter(USER_ID))
                ).stream()
                .content();
    }
}
