package com.common.jane_ai.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "com.jane.jwt")
public class JwtProperties {
//因为 Spring 无法通过反射给 static final 常量 注入配置值。
    //所以不能是常量
    public String userSecretKey;

    public long userTtl;

}
