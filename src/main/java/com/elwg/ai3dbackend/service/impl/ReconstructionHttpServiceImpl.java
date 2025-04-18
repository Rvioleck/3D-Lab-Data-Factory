package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.service.ReconstructionHttpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 3D重建HTTP通信服务实现类
 * <p>
 * 使用OkHttp客户端实现与Python重建服务的HTTP通信
 * </p>
 */
@Slf4j
@Service
public class ReconstructionHttpServiceImpl implements ReconstructionHttpService {

    @Value("${reconstruction.http.server.url:http://10.0.0.2:8001/generate3d}")
    private String serverUrl;

    @Value("${reconstruction.http.connection.timeout:30}")
    private int connectionTimeout;

    @Value("${reconstruction.http.read.timeout:300}")
    private int readTimeout;

    @Value("${reconstruction.http.write.timeout:30}")
    private int writeTimeout;

    @Value("${reconstruction.http.health.url:http://10.0.0.2:8001/health}")
    private String healthUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient client;

    /**
     * 初始化OkHttp客户端
     */
    @PostConstruct
    public void init() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
        
        log.info("Initialized HTTP reconstruction service with server URL: {}", serverUrl);
    }

    /**
     * 发送图片数据到Python服务进行3D重建
     *
     * @param imageData 图片二进制数据
     * @param taskId 任务ID
     * @param callbackUrl 回调URL，Python服务处理完成后将调用此URL
     * @return 包含请求状态的CompletableFuture
     * @throws IOException 如果通信过程中发生错误
     */
    @Override
    public CompletableFuture<String> sendImageForReconstruction(byte[] imageData, String taskId, String callbackUrl) throws IOException {
        log.info("Sending image data for task: {}, size: {} bytes", taskId, imageData.length);
        
        // 创建请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("task_id", taskId)
                .addFormDataPart("callback_url", callbackUrl)
                .addFormDataPart("image", "image.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                .build();
        
        // 创建请求
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();
        
        // 创建CompletableFuture
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Failed to send image data for task: {}", taskId, e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorMessage = responseBody != null ? responseBody.string() : "Unknown error";
                        log.error("Server returned error for task: {}, code: {}, message: {}", 
                                taskId, response.code(), errorMessage);
                        future.completeExceptionally(new IOException("Server returned error: " + response.code() + " " + errorMessage));
                        return;
                    }
                    
                    if (responseBody == null) {
                        log.error("Server returned empty response for task: {}", taskId);
                        future.completeExceptionally(new IOException("Server returned empty response"));
                        return;
                    }
                    
                    String responseString = responseBody.string();
                    log.info("Received response for task: {}: {}", taskId, responseString);
                    
                    // 解析响应
                    try {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> responseMap = objectMapper.readValue(responseString, java.util.Map.class);
                        String status = (String) responseMap.get("status");
                        
                        if ("accepted".equals(status)) {
                            log.info("Task accepted by server: {}", taskId);
                            future.complete("accepted");
                        } else {
                            log.warn("Server returned unexpected status for task: {}: {}", taskId, status);
                            future.complete(status);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse server response for task: {}", taskId, e);
                        future.completeExceptionally(e);
                    }
                }
            }
        });
        
        return future;
    }

    /**
     * 检查Python服务健康状态
     *
     * @return 如果服务正常运行，则返回true
     */
    @Override
    public boolean checkServiceHealth() {
        try {
            Request request = new Request.Builder()
                    .url(healthUrl)
                    .get()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("Failed to check service health", e);
            return false;
        }
    }
}
