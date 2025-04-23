package com.elwg.ai3dbackend.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP文件工具类
 * <p>
 * 提供ZIP文件的解压缩等功能
 * </p>
 */
@Slf4j
public class ZipUtils {

    /**
     * 解压ZIP文件
     *
     * @param zipData ZIP文件数据
     * @return 文件名到文件数据的映射
     * @throws IOException 如果解压过程中发生错误
     */
    public static Map<String, byte[]> unzip(byte[] zipData) throws IOException {
        Map<String, byte[]> extractedFiles = new HashMap<>();
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
             ZipInputStream zis = new ZipInputStream(bais)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    
                    // 读取文件内容
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    extractedFiles.put(fileName, baos.toByteArray());
                    log.info("Extracted file from ZIP: {}, size: {} bytes", fileName, baos.size());
                }
                zis.closeEntry();
            }
        }
        
        return extractedFiles;
    }
    
    /**
     * 解压ZIP文件
     *
     * @param inputStream ZIP文件输入流
     * @return 文件名到文件数据的映射
     * @throws IOException 如果解压过程中发生错误
     */
    public static Map<String, byte[]> unzip(InputStream inputStream) throws IOException {
        Map<String, byte[]> extractedFiles = new HashMap<>();
        
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    
                    // 读取文件内容
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    extractedFiles.put(fileName, baos.toByteArray());
                    log.info("Extracted file from ZIP: {}, size: {} bytes", fileName, baos.size());
                }
                zis.closeEntry();
            }
        }
        
        return extractedFiles;
    }
    
    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名，如果没有则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        
        return "";
    }
}
