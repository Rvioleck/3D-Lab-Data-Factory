package com.elwg.ai3dbackend.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 3D重建HTTP通信服务接口
 * <p>
 * 提供与Python重建服务的HTTP通信功能，用于发送图片数据和接收处理结果的回调
 * </p>
 */
public interface ReconstructionHttpService {

    /**
     * 发送图片数据到Python服务进行3D重建
     *
     * @param imageData 图片二进制数据
     * @param taskId 任务ID
     * @param callbackUrl 回调URL，Python服务处理完成后将调用此URL
     * @return 包含请求状态的CompletableFuture
     * @throws IOException 如果通信过程中发生错误
     */
    CompletableFuture<String> sendImageForReconstruction(byte[] imageData, String taskId, String callbackUrl) throws IOException;

    /**
     * 检查Python服务健康状态
     *
     * @return 如果服务正常运行，则返回true
     */
    boolean checkServiceHealth();
}
