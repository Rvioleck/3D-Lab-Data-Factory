package com.elwg.ai3dbackend.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Jackson配置类
 * 用于配置全局JSON序列化规则
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 配置ObjectMapper
     * 添加自定义序列化器，包括Long类型和Date类型的处理
     *
     * @param builder Jackson2ObjectMapperBuilder
     * @return 配置好的ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // 创建ObjectMapper，并设置日期格式
        ObjectMapper objectMapper = builder
                .createXmlMapper(false)
                .dateFormat(new SimpleDateFormat(DATE_FORMAT))
                .build();

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

        // 添加Date类型反序列化器，支持多种格式
        simpleModule.addDeserializer(Date.class, new StdDeserializer<Date>(Date.class) {
            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateStr = p.getText().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }

                // 尝试多种日期格式
                SimpleDateFormat[] dateFormats = {
                    new SimpleDateFormat(DATE_FORMAT),                // yyyy-MM-dd HH:mm:ss
                    new SimpleDateFormat("yyyy-MM-dd"),              // yyyy-MM-dd
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),  // ISO-8601 without timezone
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") // ISO-8601 with millis
                };

                // 尝试解析日期
                for (SimpleDateFormat dateFormat : dateFormats) {
                    try {
                        return dateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        // 继续尝试下一个格式
                    }
                }

                // 如果所有格式都失败，抛出异常
                throw new IOException("无法解析日期: " + dateStr);
            }
        });

        // 注册模块
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}
