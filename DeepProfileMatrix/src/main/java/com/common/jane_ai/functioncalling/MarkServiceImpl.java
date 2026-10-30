package com.common.jane_ai.functioncalling;

import com.common.jane_ai.service.impl.AgentSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class MarkServiceImpl {

    @Autowired
    private AgentSystemService agentSystemService;

    /** 旧 Agent 与 MCP 共用评分保存逻辑，userId 必须来自服务端工具上下文。 */
    public String mark(Double score, long userId) {
        if (userId <= 0) {
            return "上传失败，请先登录 Jane 系统";
        }
        if (score == null || !Double.isFinite(score) || score < 0 || score > 100) {
            return "上传失败，评分必须在 0 到 100 之间";
        }
        // 由事务服务处理失败，不再吞掉数据库异常后返回上传成功。
        agentSystemService.saveScoreForUser(BigDecimal.valueOf(score), userId);
        log.info("用户评分保存成功，userId={}", userId);
        return "上传成功";
    }
}
