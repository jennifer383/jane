package com.common.jane_ai.caffine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PDFlocalCache {

    private Cache<String, String> pdfFileCache;


    @PostConstruct
    public void init() {
        pdfFileCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }

    public String getpdfFile(String key) {
        return pdfFileCache.get(key, k -> "");
    }

    public void putpdfFile(String key, String text){
        pdfFileCache.put(key, text);
    }
}
