package com.elwg.ai3dbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API 服务
 * 使用原生OkHttp实现，直接调用DeepSeek API
 */
@Slf4j
@Service
public class DeepSeekService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    @Value("${deepseek.api.temperature:0.7}")
    private double temperature;

    @Value("${deepseek.api.max-tokens:2048}")
    private int maxTokens;

    @Resource(name = "deepSeekOkHttpClient")
    private OkHttpClient okHttpClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 同步调用DeepSeek API
     *
     * @param messages 消息列表
     * @return AI响应内容
     */
    public String chatCompletion(List<Map<String, String>> messages) {
        log.info("DeepSeek API call with {} messages", messages.size());

        // 合并连续的同类型消息
        List<Map<String, String>> mergedMessages = mergeConsecutiveMessages(messages);
        log.info("After merging, {} messages remain", mergedMessages.size());

        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("messages", objectMapper.valueToTree(mergedMessages));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            // 创建请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // 发送请求并处理响应
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                JsonNode jsonResponse = objectMapper.readTree(response.body().string());
                return jsonResponse.path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();
            }
        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            throw new RuntimeException("Failed to get AI response", e);
        }
    }

    /**
     * 合并连续的同类型消息
     * 在API调用前合并连续的user或assistant消息
     *
     * @param messages 原始消息列表
     * @return 合并后的消息列表
     */
    private List<Map<String, String>> mergeConsecutiveMessages(List<Map<String, String>> messages) {
        if (messages == null || messages.size() < 2) {
            return messages;
        }

        List<Map<String, String>> result = new ArrayList<>();
        Map<String, String> currentMessage = new HashMap<>(messages.get(0));
        result.add(currentMessage);

        for (int i = 1; i < messages.size(); i++) {
            Map<String, String> nextMessage = messages.get(i);

            // 如果当前消息和下一条消息角色相同，合并内容
            if (currentMessage.get("role").equals(nextMessage.get("role"))) {
                String mergedContent = currentMessage.get("content") + "\n\n" + nextMessage.get("content");
                currentMessage.put("content", mergedContent);
                log.debug("Merged consecutive {} messages", currentMessage.get("role"));
            } else {
                // 角色不同，添加为新消息
                currentMessage = new HashMap<>(nextMessage);
                result.add(currentMessage);
            }
        }

        return result;
    }

    /**
     * 流式调用DeepSeek API
     *
     * @param messages 消息列表
     * @param emitter SSE发射器
     */
    public void streamChatCompletion(List<Map<String, String>> messages, SseEmitter emitter) {
        log.info("DeepSeek Stream API call with {} messages", messages.size());

        // 合并连续的同类型消息
        List<Map<String, String>> mergedMessages = mergeConsecutiveMessages(messages);
        log.info("After merging, {} messages remain for streaming", mergedMessages.size());

        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("messages", objectMapper.valueToTree(mergedMessages));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("stream", true);

            // 创建请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Accept", "text/event-stream")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // 异步发送请求
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Stream request failed", e);
                    emitter.completeWithError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            throw new IOException("Unexpected response: " + response);
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream())
                        );

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) continue;

                            if (line.equals("data: [DONE]")) {
                                emitter.complete();
                                break;
                            }

                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6);
                                try {
                                    JsonNode json = objectMapper.readTree(jsonData);
                                    String content = json.path("choices")
                                            .path(0)
                                            .path("delta")
                                            .path("content")
                                            .asText("");

                                    if (!content.isEmpty()) {
                                        // 直接发送纯文本内容，不使用事件包装
                                        emitter.send(content);
                                        // 打印日志以便调试
                                        log.debug("Sending content chunk: {}", content);
                                    }
                                } catch (Exception e) {
                                    log.error("Error parsing JSON: {}", jsonData, e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error in stream response", e);
                        emitter.completeWithError(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error setting up stream", e);
            emitter.completeWithError(e);
        }
    }
}