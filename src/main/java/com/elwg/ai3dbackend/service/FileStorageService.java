package com.elwg.ai3dbackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件存储服务接口
 * <p>
 * 提供文件存储、读取、删除等功能
 * </p>
 */
public interface FileStorageService {

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
     * 获取文件URL
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的URL
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
     * 列出目录中的所有子目录
     *
     * @param directory 目录路径（相对于存储根目录）
     * @return 子目录名列表
     */
    List<String> listDirectories(String directory);

    /**
     * 获取文件的物理路径
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的物理路径，如果不适用则返回null
     */
    String getPhysicalPath(String path);
}
