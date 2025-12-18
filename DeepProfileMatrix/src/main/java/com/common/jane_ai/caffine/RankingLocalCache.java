package com.common.jane_ai.caffine;

import com.common.jane_ai.Obj.vo.PDFVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
public class RankingLocalCache {

    private Cache<String, List<PDFVO>> rankingCache;

    @PostConstruct
    public void init() {
        rankingCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }

    public List<PDFVO> getRanking(String key) {
        return rankingCache.get(key, k -> new ArrayList<>());
    }

    public void putRanking(String key, List<PDFVO> list){
        rankingCache.put(key, list);
    }

    public void removeRanking(String key){
        rankingCache.invalidate(key);
    }

}







