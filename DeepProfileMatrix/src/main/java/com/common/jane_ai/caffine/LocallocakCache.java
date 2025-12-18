package com.common.jane_ai.caffine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//本地锁缓存
@Component
public class LocallocakCache {

    private Cache<String, ReentrantLock>  lockcache;

    @PostConstruct
    public void init(){
        lockcache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(48, TimeUnit.HOURS)
                .build();
    }

    //Caffeine 使用的是无锁或低锁的高并发结构，线程安全。
    public ReentrantLock getLock(String key){
        return lockcache.get(key, k -> new ReentrantLock());
    }
}
