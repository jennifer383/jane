package com.common.jane_ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cors")
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

    private List<String> allowedOrigins;//允许的源IP
    private List<String> allowedMethods;//允许的方法
    private List<String> allowedHeaders;//允许的头部
    private boolean allowCredentials;//允许携带认证信息
    private long maxAge;//预检请求缓存时间

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 添加全局的跨域请求映射配置
        registry.addMapping("/**") // 所有路径都可以跨域
                // 只允许配置文件中指定的源地址，提高安全性
                .allowedOrigins(allowedOrigins != null && !allowedOrigins.isEmpty()
                    ? allowedOrigins.toArray(new String[0])
                    : new String[]{"http://localhost", "http://localhost:8080"}) // 只允许指定的两个源IP
                .allowedMethods(allowedMethods != null && !allowedMethods.isEmpty()
                    ? allowedMethods.toArray(new String[0])
                    : new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"}) // 从配置文件读取允许的方法
                .allowedHeaders(allowedHeaders != null && !allowedHeaders.isEmpty()
                    ? allowedHeaders.toArray(new String[0])
                    : new String[]{"*"}) // 从配置文件读取允许的头部
                .allowCredentials(allowCredentials) // 允许携带认证信息
                .maxAge(maxAge > 0 ? maxAge : 3600) // 预检请求缓存时间
                .exposedHeaders("Content-Disposition", "Authorization"); // 暴露的头部信息
    }

    //在Spring Boot等Java框架中，通过YAML或Properties配置文件注入配置值到类中时，通常需要为这些配置属性提供getter和setter方法。
    // 这是因为框架通常使用JavaBean的规范来注入属性值。
    // Getter and Setter methods
    //构造方法方便容器通过反射去配置值
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
