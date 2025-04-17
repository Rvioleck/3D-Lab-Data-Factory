package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地存储服务实现
 * <p>
 * 使用本地文件系统实现文件存储功能
 * </p>
 */
@Slf4j
@Service("localStorageService")
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.root-path:./storage}")
    private String rootPath;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * 初始化存储目录
     */
    @PostConstruct
    @Override
    public void init() throws IOException {
        try {
            Path storagePath = Paths.get(rootPath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Created local storage directory: {}", storagePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create local storage directory", e);
            throw e;
        }
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
        Path filePath = getFullPath(path);
        
        // 确保父目录存在
        Files.createDirectories(filePath.getParent());
        
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(data);
        }
        log.info("Saved file to local storage: {}", filePath);
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
        Path filePath = getFullPath(path);
        
        // 确保父目录存在
        Files.createDirectories(filePath.getParent());
        
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        log.info("Saved file to local storage: {}", filePath);
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
        Path filePath = getFullPath(path);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IOException("File not found: " + path);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * 获取文件的访问URL
     * <p>
     * 对于本地存储，返回带有特殊前缀的绝对路径，以便前端能够区分
     * </p>
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的访问URL
     */
    @Override
    public String getFileUrl(String path) {
        Path filePath = getFullPath(path);
        String absolutePath = filePath.toAbsolutePath().toString();
        
        // 添加特殊前缀，以便前端能够识别这是一个绝对路径
        return "ABSOLUTE_PATH:" + absolutePath;
        
        // 如果需要使用相对URL路径，可以使用下面的代码
        /*
        String contextPathStr = contextPath;
        if (!contextPathStr.startsWith("/")) {
            contextPathStr = "/" + contextPathStr;
        }
        if (contextPathStr.endsWith("/")) {
            contextPathStr = contextPathStr.substring(0, contextPathStr.length() - 1);
        }
        
        // 假设有一个专门的控制器端点用于访问文件
        return contextPathStr + "/storage/files/" + path;
        */
    }

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果文件存在，则返回true
     */
    @Override
    public boolean isFileExists(String path) {
        Path filePath = getFullPath(path);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    /**
     * 删除文件
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 如果删除成功，则返回true
     */
    @Override
    public boolean deleteFile(String path) {
        try {
            Path filePath = getFullPath(path);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", path, e);
            return false;
        }
    }

    /**
     * 列出目录中的所有文件
     *
     * @param directory 目录路径（相对于存储根目录）
     * @return 文件名到文件路径的映射
     */
    @Override
    public Map<String, String> listFiles(String directory) {
        Map<String, String> files = new HashMap<>();
        
        Path dirPath = getFullPath(directory);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return files;
        }
        
        try {
            Files.list(dirPath).forEach(path -> {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    files.put(fileName, path.toString());
                }
            });
        } catch (IOException e) {
            log.error("Failed to list files in directory: {}", directory, e);
        }
        
        return files;
    }
    
    /**
     * 获取文件的物理路径
     *
     * @param path 文件路径（相对于存储根目录）
     * @return 文件的物理路径
     */
    @Override
    public String getPhysicalPath(String path) {
        return getFullPath(path).toString();
    }
    
    /**
     * 获取文件的完整路径
     *
     * @param relativePath 相对路径
     * @return 完整的文件路径
     */
    private Path getFullPath(String relativePath) {
        return Paths.get(rootPath, relativePath);
    }
}
