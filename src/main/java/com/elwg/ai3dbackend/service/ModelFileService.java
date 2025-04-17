package com.elwg.ai3dbackend.service;

import com.elwg.ai3dbackend.model.dto.ReconstructionResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 3D模型文件服务接口
 * <p>
 * 提供3D模型文件的处理和存储功能，支持不同的存储实现
 * </p>
 */
public interface ModelFileService {

    /**
     * 处理3D重建结果部分
     * <p>
     * 处理渐进式接收的结果部分，并存储到指定的存储服务
     * </p>
     *
     * @param taskId 任务ID
     * @param name 结果部分名称（例如：pixel_images.png）
     * @param contentType 内容类型
     * @param data 二进制数据
     * @return 包含处理结果的响应对象
     * @throws IOException 如果文件处理过程中发生错误
     */
    ReconstructionResponse processResultPart(String taskId, String name, String contentType, byte[] data) throws IOException;

    /**
     * 处理3D重建结果ZIP文件（旧方法，保留向后兼容性）
     * <p>
     * 解析ZIP文件，提取其中的各个文件，并存储到指定的存储服务
     * </p>
     *
     * @param taskId 任务ID
     * @param zipData ZIP文件的二进制数据
     * @return 包含各个文件URL的响应对象
     * @throws IOException 如果文件处理过程中发生错误
     * @deprecated 使用 {@link #processResultPart(String, String, String, byte[])} 代替
     */
    @Deprecated
    ReconstructionResponse processReconstructionResult(String taskId, byte[] zipData) throws IOException;

    /**
     * 获取文件的物理路径（如果适用）
     *
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 文件的物理路径
     */
    String getFilePath(String taskId, String fileName);

    /**
     * 获取文件的URL路径
     *
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 文件的URL路径
     */
    String getFileUrl(String taskId, String fileName);

    /**
     * 检查任务结果是否存在
     *
     * @param taskId 任务ID
     * @return 如果任务结果存在，则返回true
     */
    boolean isTaskResultExists(String taskId);

    /**
     * 检查任务是否完成
     * <p>
     * 完成意味着所有必要的结果部分都已经接收并处理
     * </p>
     *
     * @param taskId 任务ID
     * @return 如果任务已完成，则返回true
     */
    boolean isTaskCompleted(String taskId);

    /**
     * 获取任务的当前状态
     *
     * @param taskId 任务ID
     * @return 任务状态（processing, completed, failed）
     */
    String getTaskStatus(String taskId);

    /**
     * 获取任务的所有文件
     *
     * @param taskId 任务ID
     * @return 文件名到文件路径的映射
     */
    Map<String, String> getTaskFiles(String taskId);

    /**
     * 获取文件数据
     *
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 文件数据的字节数组
     * @throws IOException 如果读取过程中发生错误
     */
    byte[] getFileData(String taskId, String fileName) throws IOException;
}
