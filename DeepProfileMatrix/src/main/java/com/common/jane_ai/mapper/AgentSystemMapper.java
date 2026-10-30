package com.common.jane_ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author by蒋
 * @Description TODO 按登录用户隔离 Agent 账户和简历评分数据访问
 * @date 2026/10/29 16:21
 */
@Mapper
public interface AgentSystemMapper {
    Map<String, Object> account(@Param("userId") long userId);

    Long lockAccount(@Param("userId") long userId);

    Map<String, Object> currentScore(@Param("userId") long userId);

    int insertScore(@Param("id") long id, @Param("userId") long userId, @Param("score") BigDecimal score);

    int updateScore(@Param("id") long id, @Param("userId") long userId, @Param("score") BigDecimal score);

}
