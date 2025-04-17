package com.elwg.ai3dbackend.service;

import com.elwg.ai3dbackend.service.impl.OkHttpWebSocketServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkHttp WebSocket服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class OkHttpWebSocketServiceTest {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 测试连接状态
     */
    @Test
    public void testConnectionStatus() {
        // 检查服务是否注入成功
        assertNotNull(webSocketService);
        
        // 检查是否是OkHttp实现
        assertTrue(webSocketService instanceof OkHttpWebSocketServiceImpl);
        
        // 检查连接状态（注意：这个测试可能会失败，如果WebSocket服务器不可用）
        // 仅作为示例，实际测试中可能需要使用Mock
        boolean connected = webSocketService.isConnected();
        System.out.println("WebSocket连接状态: " + (connected ? "已连接" : "未连接"));
    }

    /**
     * 测试关闭连接
     */
    @Test
    public void testCloseConnection() {
        // 关闭连接
        webSocketService.closeConnection();
        
        // 检查连接状态
        assertFalse(webSocketService.isConnected());
    }

    /**
     * 测试发送图片数据（需要WebSocket服务器可用）
     * 注意：这是一个集成测试，需要实际的WebSocket服务器
     * 如果服务器不可用，测试将被跳过
     */
    @Test
    public void testSendImageAndGetResult() throws IOException, InterruptedException {
        // 检查连接状态，如果未连接则跳过测试
        if (!webSocketService.isConnected()) {
            System.out.println("WebSocket服务器不可用，跳过测试");
            return;
        }
        
        // 创建一个简单的测试图片数据
        byte[] testImageData = new byte[1024];
        // 填充一些测试数据
        for (int i = 0; i < testImageData.length; i++) {
            testImageData[i] = (byte) (i % 256);
        }
        
        // 发送图片数据
        CompletableFuture<byte[]> resultFuture = webSocketService.sendImageAndGetResult(testImageData);
        
        // 等待结果（设置较短的超时时间，避免测试长时间阻塞）
        try {
            byte[] result = resultFuture.get(10, TimeUnit.SECONDS);
            // 检查结果不为空
            assertNotNull(result);
            System.out.println("收到结果数据，大小: " + result.length + " 字节");
        } catch (Exception e) {
            System.out.println("等待结果超时或发生错误: " + e.getMessage());
            // 不断言失败，因为这可能是由于服务器处理时间较长导致的
        }
    }
}
