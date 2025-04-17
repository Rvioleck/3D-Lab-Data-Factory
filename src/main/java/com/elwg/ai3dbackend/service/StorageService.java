package com.elwg.ai3dbackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 存储服务接口
 * <p>
 * 定义通用的文件存储操作，支持不同的存储实现（本地存储、云存储等）
 * </p>
 */
public interface StorageService {

    /**
     * 初始化存储服务
     * 
     * @throws IOException 如果初始化过程中发生错误
     */
    void init() throws IOException;

    /**
     * 保存文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @param data 文件数据
     * @throws IOException 如果保存过程中发生错误
     */
    void saveFile(String path, byte[] data) throws IOException;

    /**
     * 保存文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @param inputStream 文件输入流
     * @throws IOException 如果保存过程中发生错误
     */
    void saveFile(String path, InputStream inputStream) throws IOException;

    /**
     * 获取文件数据
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件数据的字节数组
     * @throws IOException 如果读取过程中发生错误
     */
    byte[] getFileData(String path) throws IOException;

    /**
     * 获取文件的访问URL
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的访问URL
     */
    String getFileUrl(String path);

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果文件存在，则返回true
     */
    boolean isFileExists(String path);

    /**
     * 删除文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果删除成功，则返回true
     */
    boolean deleteFile(String path);

    /**
     * 列出目录中的所有文件
     *
     * @param directory 目录路径（相对于存储根目录）
     * @return 文件名到文件路径的映射
     */
    Map<String, String> listFiles(String directory);
    
    /**
     * 获取文件的物理路径（如果适用）
     * 对于本地存储，返回文件的实际路径；对于云存储，可能返回null或临时路径
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的物理路径，如果不适用则返回null
     */
    String getPhysicalPath(String path);
}
