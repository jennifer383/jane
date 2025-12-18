package com.common.jane_ai.common;

import com.common.jane_ai.config.BloomFilterProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BloomFilterUtils {
//配置中命名要匹配，不能有D

    private final RBloomFilter<String> bloomFilter;

    public BloomFilterUtils(RedissonClient redissonClient,BloomFilterProperties bloomFilterProperties){
        //获得布隆过滤器的名字
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomFilterProperties.getBloomFilterName());
        //初始化布隆过滤器，设置预期插入数量和误判率
        bloomFilter.tryInit(bloomFilterProperties.getExpectedInsertions(), bloomFilterProperties.getFalseProbability());
        this.bloomFilter = bloomFilter;
    }

    public boolean add(String value){
        return bloomFilter.add(value);
    }

    public boolean contains(String value){
        return bloomFilter.contains(value);
    }
}
