package com.common.jane_ai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.jane_ai.caffine.RankingLocalCache;
import com.common.jane_ai.constant.JaneRedisKeyConstant;
import com.common.jane_ai.mapper.AgentSystemMapper;
import com.common.jane_ai.service.PDFService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

/**
 * @author by蒋
 * @Description TODO 为 Harness 提供无需调用大模型的 Jane 业务操作
 * @date 2026/10/29 16:22
 */
@Service
@RequiredArgsConstructor
public class AgentSystemService {
    private final AgentSystemMapper mapper;
    private final RankingLocalCache rankingCache;
    private final PDFService pdfService;

    private long userId() {
        String value = BaseParameterHolder.getBaseParameter(USER_ID);
        if (value == null || !value.matches("[1-9][0-9]{0,18}")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录 Jane 系统");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录用户标识无效");
        }
    }

    public Map<String, Object> account() {
        Map<String, Object> account = mapper.account(userId());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录账号不存在");
        }
        return account;
    }

    public String extractResume(MultipartFile file) {
        userId();
        if (file == null || file.isEmpty() || file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择不超过 10MB 的 PDF 简历");
        }
        // 仅提取文本，评分和推理由 Harness 完成，不调用 ChatClient 或向量服务。
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.getNumberOfPages() > 50 || !document.getCurrentAccessPermission().canExtractContent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 超过 50 页或不允许提取文本");
            }
            String text = new PDFTextStripper().getText(document);
            if (text.isBlank() || text.length() > 100000) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 无可提取文本或文本超过 10 万字符，请使用文本型简历");
            }
            return text;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无法解析 PDF，请检查文件是否损坏或加密");
        }
    }

    public Map<String, Object> score() {
        Map<String, Object> score = mapper.currentScore(userId());
        return score == null ? Map.of("hasScore", false) : score;
    }

    @Transactional
    public Map<String, Object> saveScore(BigDecimal score) {
        return saveScoreForUser(score, userId());
    }

    /** 内部工具使用请求开始时捕获的认证身份，避免依赖异步线程中的 ThreadLocal。 */
    @Transactional
    public Map<String, Object> saveScoreForUser(BigDecimal score, long userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录 Jane 系统");
        }
        if (score == null || score.signum() < 0 || score.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评分必须在 0 到 100 之间");
        }
        // 锁定账号以串行化同一用户的提交，避免首次评分并发创建多条记录。
        if (mapper.lockAccount(userId) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录账号不存在");
        }
        Map<String, Object> existing = mapper.currentScore(userId);
        if (existing != null && existing.get("score") != null
                && new BigDecimal(existing.get("score").toString()).compareTo(score) == 0) {
            return existing;
        }
        int affected = existing == null
                ? mapper.insertScore(IdWorker.getId(), userId, score)
                : mapper.updateScore(((Number) existing.get("id")).longValue(), userId, score);
        if (affected != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "评分保存失败，请重新查询后再提交");
        }
        rankingCache.removeRanking(JaneRedisKeyConstant.JANE_RANKING);
        return mapper.currentScore(userId);
    }

    public List<Map<String, Object>> rankings() {
        userId();
        // 与网页复用同一榜单及缓存，保留历史评分记录的姓名，不按登录账号合并。
        return pdfService.ranking().stream().map(record -> {
            Map<String, Object> item = new LinkedHashMap<>();
            String name = record.getUserName();
            item.put("username", name == null || name.isBlank() ? "未知用户" : name);
            item.put("score", record.getPdfScore());
            item.put("updateCount", record.getPdfUpdateCount());
            item.put("updateTime", record.getUpdateTime());
            return item;
        }).toList();
    }
}
