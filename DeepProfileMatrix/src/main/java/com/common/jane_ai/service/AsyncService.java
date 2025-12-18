package com.common.jane_ai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AsyncService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void yureprepare(Long userId) {

    }

    /**
     * 异步提取PDF文件文本
     * @param file PDF文件
     * @return 提取的文本内容
     */
    @Async
    public CompletableFuture<String> extractPDFTextAsync(MultipartFile file) {
        try {
            // 验证文件类型
            if (!file.getContentType().equals("application/pdf")) {
                return CompletableFuture.completedFuture("");
            }
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                log.info("PDF文件提取完成，文件名：{}", file.getOriginalFilename());
                return CompletableFuture.completedFuture(text);
            }
        } catch (IOException e) {
            log.error("PDF文件提取失败: {}", e.getMessage());
            return CompletableFuture.completedFuture("");
        }
    }

}
