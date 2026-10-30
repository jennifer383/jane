package com.common.jane_ai.controller;

import com.common.jane_ai.Obj.dto.AgentScoreDTO;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.service.ChatHistoryService;
import com.common.jane_ai.service.ModelService;
import com.common.jane_ai.service.impl.AgentSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * @author by蒋
 * @Description TODO 提供 Harness Agent 登录后可调用的系统业务接口
 * @date 2026/10/29 16:22
 */
@RestController
@RequestMapping("/jane/agent")
@RequiredArgsConstructor
public class AgentSystemController {
    private final AgentSystemService service;
    private final ChatHistoryService historyService;
    private final ModelService modelService;

    @GetMapping("/account")
    public ApiResponse<Map<String, Object>> account() {
        return ApiResponse.ok(service.account());
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ChatHistoryDTO>> conversations() {
        return ApiResponse.ok(historyService.getTitleHistory());
    }

    @GetMapping("/messages")
    public ApiResponse<List<Message>> messages(@RequestParam String chatId) {
        return ApiResponse.ok(historyService.getChatHistory(chatId));
    }

    @GetMapping("/model")
    public ApiResponse<Map<String, String>> model() {
        return ApiResponse.ok(Map.of("model", modelService.getCurrentModel(), "scope", "jane-server-global"));
    }

    @PostMapping("/model")
    public ApiResponse<Map<String, String>> changeModel(@RequestParam String model) {
        // 只调整 Jane 的模型设置，不改变 Harness 当前使用的模型。
        return modelService.changeModel(model) ? model() : ApiResponse.error("模型名称不支持");
    }

    @PostMapping(value = "/resume/text", consumes = "multipart/form-data")
    public ApiResponse<String> extractResume(@RequestParam MultipartFile file) {
        return ApiResponse.ok(service.extractResume(file));
    }

    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> score() {
        return ApiResponse.ok(service.score());
    }

    @PostMapping("/score")
    public ApiResponse<Map<String, Object>> saveScore(@Valid @RequestBody AgentScoreDTO request) {
        return ApiResponse.ok(service.saveScore(request.getScore()));
    }

    @GetMapping("/rankings")
    public ApiResponse<List<Map<String, Object>>> rankings() {
        return ApiResponse.ok(service.rankings());
    }
}
