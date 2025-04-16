package com.elwg.ai3dbackend.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * Jackson配置类
 * 用于配置全局JSON序列化规则
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper
     * 添加自定义序列化器，将Long类型序列化为字符串
     *
     * @param builder Jackson2ObjectMapperBuilder
     * @return 配置好的ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 创建自定义序列化模块
        SimpleModule simpleModule = new SimpleModule();
        
        // 添加Long类型序列化器
        simpleModule.addSerializer(Long.class, new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    gen.writeString(value.toString());
                } else {
                    gen.writeNull();
                }
            }
        });
        
        // 添加long类型序列化器
        simpleModule.addSerializer(long.class, new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toString());
            }
        });
        
        // 注册模块
        objectMapper.registerModule(simpleModule);
        
        return objectMapper;
    }
}
