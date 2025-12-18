package com.common.jane_ai.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类
 * 配置API文档的基本信息
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Jane AI API",
                description = "Jane AI 项目的API文档",
                version = "1.0.0",
                contact = @Contact(
                        name = "Jane AI Team",
                        email = "jane-ai@example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "本地开发环境"),
                @Server(url = "https://api.example.com", description = "生产环境")
        }
)
public class OpenApiConfig {
}