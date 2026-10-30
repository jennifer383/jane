package com.common.jane_ai.Obj.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author by蒋
 * @Description TODO 接收 Harness 对当前登录用户简历的评分
 * @date 2026/10/29 16:21
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class AgentScoreDTO {
    /** 简历评分，范围为 0 到 100 分 **/
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal score;
}
