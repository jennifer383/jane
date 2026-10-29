package com.common.jane_ai.controller;

import com.common.jane_ai.Obj.vo.PDFVO;
import com.common.jane_ai.RepateExecuteLimit.RepateExecuteLimit;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import com.common.jane_ai.service.PDFService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/jane/pdf")
public class PDFController {

    @Autowired
    private PDFService pdfService;

//    @RepateExecuteLimit(type = JaneTypeConstant.PDF_MIDENG, userId = "#userId", durationTime = 300L)
    @PostMapping(value="/upload",produces = "text/html;charset=utf-8")//30分钟幂等防止多次文档上传                 /请求体
    public Flux<String> upload(@RequestParam MultipartFile file,@RequestHeader String userId, @RequestHeader String chatId) {
        log.info("上传PDF文件-{}",chatId);
        //userId在 BasePharameter中
        return pdfService.uploadPDF(file, chatId);
        //可以设计为存储到redis或者是本地中缓存中，
//        return ApiResponse.ok(pdfvo);
    }

    @GetMapping("/ranking")
    public ApiResponse<List<PDFVO>> ranking() {
        log.info("PDF-简历得分-排序");
        return ApiResponse.ok(pdfService.ranking());
    }
}
