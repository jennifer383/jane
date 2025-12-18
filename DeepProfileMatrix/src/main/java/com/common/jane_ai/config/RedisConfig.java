package com.common.jane_ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // 键使用字符串序列化器（保持可读性）
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        // 值使用JSON序列化器
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
//}
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;






//    键（Key）序列化器 用 StringRedisSerializer，保证 Redis key 是字符串，方便查看和操作。
//
//    值（Value）序列化器 使用 GenericJackson2JsonRedisSerializer，结合了 Jackson 的 ObjectMapper，可以自动将 Java 对象序列化为 JSON 存入 Redis，读取时自动反序列化回对应类型。
//
//    同时，Hash 的 key 和 value 也用了相应的序列化器。
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);//设置 Redis 连接工厂（负责创建与 Redis 的连接


        // 键用字符串序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 值用 GenericJackson2JsonRedisSerializer（自动处理类型）
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();//初始化模板，验证配置有效性（未设置序列化器会抛出IllegalStateException
        return template;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 单节点配置，修改为你的 Redis 地址
        config.useSingleServer()
                .setAddress("redis://"+redisHost+":"+redisPort)
//                .setUsername("root")
                .setPassword(redisPassword)
                .setDatabase(0)
                .setTimeout(5000) // 连接超时时间（毫秒）
                .setPingConnectionInterval(1000); // 心跳间隔

        return Redisson.create(config);
    }
}
