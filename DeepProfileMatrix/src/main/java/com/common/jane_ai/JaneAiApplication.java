package com.common.jane_ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//@Mapper 和 @MapperScan 的作用就是 将 MyBatis 的 Mapper 接口注册到 Spring 容器中
//这样就可以在 Service 中通过 @Autowired 或构造函数注入这些 Mapper 对象。
@SpringBootApplication
@EnableScheduling
@MapperScan("com.common.jane_ai.mapper")
public class JaneAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaneAiApplication.class, args);
    }

}
