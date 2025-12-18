package com.common.jane_ai.config;

import com.github.benmanes.caffeine.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Caffeine 缓存配置类
 * 统一管理所有缓存配置，便于监控和维护
 */
@Configuration
public class CaffeineConfig {

    private static final Logger log = LoggerFactory.getLogger(CaffeineConfig.class);

    /**
     * 本地锁缓存
     * 用于存储分布式锁的本地副本，减少分布式锁的请求次数
     */
    @Bean("lockCache")
    public Cache<String, ReentrantLock> lockCache() {
        return Caffeine.newBuilder()
                .maximumSize(500) // 增加最大大小，适应更多并发场景
                .expireAfterWrite(48, TimeUnit.HOURS)
                .recordStats() // 启用统计功能
                .removalListener((key, value, cause) -> {
                    log.debug("锁缓存移除: key={}, cause={}", key, cause);
                })
                .build();
    }

    /**
     * PDF文件内容缓存
     * 用于缓存PDF文件的提取内容，减少重复提取操作
     */
    @Bean("pdfFileCache")
    public Cache<String, String> pdfFileCache() {
        return Caffeine.newBuilder()
                .maximumSize(200) // 增加最大大小，缓存更多PDF内容
                .expireAfterWrite(12, TimeUnit.HOURS) // 缩短过期时间，释放内存
                .recordStats() // 启用统计功能
                .removalListener((key, value, cause) -> {
                    log.debug("PDF缓存移除: key={}, cause={}", key, cause);
                })
                .build();
    }

    /**
     * 排名数据缓存
     * 用于缓存排名数据，减少数据库查询
     */
    @Bean("rankingCache")
    public Cache<String, List<Object>> rankingCache() {
        return Caffeine.newBuilder()
                .maximumSize(200) // 增加最大大小
                .expireAfterWrite(6, TimeUnit.HOURS) // 缩短过期时间，保持数据新鲜
                .recordStats() // 启用统计功能
                .removalListener((key, value, cause) -> {
                    log.debug("排名缓存移除: key={}, cause={}", key, cause);
                })
                .build();
    }

    /**
     * 模型配置缓存
     * 用于缓存AI模型的配置信息，减少配置读取次数
     */
    @Bean("modelConfigCache")
    public Cache<String, String> modelConfigCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("模型配置缓存移除: key={}, cause={}", key, cause);
                })
                .build();
    }

    /**
     * 用户会话缓存
     * 用于缓存用户会话信息，减少Redis查询
     */
    @Bean("userSessionCache")
    public Cache<String, Object> userSessionCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(1, TimeUnit.HOURS) // 基于访问时间过期
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("用户会话缓存移除: key={}, cause={}", key, cause);
                })
                .build();
    }
}