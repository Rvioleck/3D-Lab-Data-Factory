package com.elwg.ai3dbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类
 * <p>
 * 配置 SpringDoc OpenAPI 3.0 文档生成器，用于生成 API 文档。
 * 该配置类定义了 API 文档的基本信息，如标题、描述、版本等。
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 3D Backend API")
                        .description("AI 3D Backend 项目的 API 文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ELWG")
                                .email("support@elwg.com")
                                .url("https://github.com/Rvioleck/ai-3d-backend")));
    }
}
