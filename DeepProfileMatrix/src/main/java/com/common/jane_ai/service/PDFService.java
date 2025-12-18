package com.common.jane_ai.service;

import com.common.jane_ai.Obj.vo.PDFVO;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface PDFService {


    Flux<String> uploadPDF(MultipartFile file, String chatId);

    List<PDFVO> ranking();
}
