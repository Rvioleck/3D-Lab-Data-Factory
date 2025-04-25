package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.manager.CosManager;
import com.elwg.ai3dbackend.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 腾讯云对象存储服务实现
 * <p>
 * 使用腾讯云COS实现文件存储功能
 * </p>
 */
@Slf4j
@Service("cosFileStorageService")
public class CosFileStorageServiceImpl implements FileStorageService {

    @Autowired
    private CosManager cosManager;

    /**
     * 初始化存储服务
     * CosManager已经在初始化时检查了存储桶是否存在
     */
    @PostConstruct
    @Override
    public void init() throws IOException {
        // CosManager已经在初始化时检查了存储桶是否存在
        log.info("Initialized Tencent COS storage service");
    }

    /**
     * 保存文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @param data 文件数据
     * @throws IOException 如果保存过程中发生错误
     */
    @Override
    public void saveFile(String path, byte[] data) throws IOException {
        cosManager.uploadFile(path, data);
    }

    /**
     * 保存文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @param inputStream 文件输入流
     * @throws IOException 如果保存过程中发生错误
     */
    @Override
    public void saveFile(String path, InputStream inputStream) throws IOException {
        cosManager.uploadFile(path, inputStream);
    }

    /**
     * 获取文件数据
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件数据的字节数组
     * @throws IOException 如果读取过程中发生错误
     */
    @Override
    public byte[] getFileData(String path) throws IOException {
        return cosManager.downloadFile(path);
    }

    /**
     * 获取文件URL
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的URL
     */
    @Override
    public String getFileUrl(String path) {
        return cosManager.getFileUrl(path);
    }

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果文件存在，则返回true
     */
    @Override
    public boolean isFileExists(String path) {
        return cosManager.isFileExists(path);
    }

    /**
     * 删除文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果删除成功，则返回true
     */
    @Override
    public boolean deleteFile(String path) {
        return cosManager.deleteFile(path);
    }

    /**
     * 列出目录中的所有文件
     *
     * @param directory 目录路径（相对于存储根目录）
     * @return 文件名到文件路径的映射
     */
    @Override
    public Map<String, String> listFiles(String directory) {
        return cosManager.listFiles(directory);
    }

    /**
     * 列出目录中的所有子目录
     *
     * @param directory 目录路径（相对于存储根目录）
     * @return 子目录名列表
     */
    @Override
    public List<String> listDirectories(String directory) {
        return cosManager.listDirectories(directory);
    }

    /**
     * 获取文件的物理路径
     * 对于云存储，不适用物理路径的概念，返回null
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 始终返回null，因为COS没有物理路径
     */
    @Override
    public String getPhysicalPath(String path) {
        // 对于云存储，不适用物理路径的概念
        return null;
    }
}
