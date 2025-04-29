package com.elwg.ai3dbackend.manager;

import com.elwg.ai3dbackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 腾讯云对象存储管理器
 * <p>
 * 封装腾讯云COS的操作，提供统一的接口
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CosManager {

    private final COSClient cosClient;

    private final CosClientConfig cosConfig;

    /**
     * 初始化COS管理器
     * 检查存储桶是否存在
     */
    @PostConstruct
    public void init() {
        String bucketName = cosConfig.getBucket();
        try {
            if (!cosClient.doesBucketExist(bucketName)) {
                log.warn("Bucket {} does not exist, please create it in Tencent Cloud Console", bucketName);
            } else {
                log.info("Connected to Tencent COS bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Tencent COS manager", e);
        }
    }


    /**
     * 上传文件
     *
     * @param key 对象键（文件路径）
     * @param inputStream 文件输入流
     * @throws IOException 如果上传过程中发生错误
     */
    public void uploadFile(String key, InputStream inputStream) throws IOException {
        try {
            // 标准化路径，去除开头的斜杠
            key = normalizePath(key);

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucket(), key, inputStream, metadata);

            // 执行上传
            cosClient.putObject(putObjectRequest);
            log.info("Uploaded file to Tencent COS: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file to Tencent COS: {}", key, e);
            throw new IOException("Failed to upload file to Tencent COS", e);
        }
    }

    /**
     * 下载文件
     *
     * @param key 对象键（文件路径）
     * @return 文件数据的字节数组
     * @throws IOException 如果下载过程中发生错误
     */
    public byte[] downloadFile(String key) throws IOException {
        key = normalizePath(key);

        try {
            // 获取对象
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosConfig.getBucket(), key);
            COSObject cosObject = cosClient.getObject(getObjectRequest);

            // 读取对象内容
            try (InputStream inputStream = cosObject.getObjectContent();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("Failed to download file from Tencent COS: {}", key, e);
            throw new IOException("Failed to download file from Tencent COS", e);
        }
    }

    /**
     * 获取文件的访问URL
     *
     * @param key 对象键（文件路径）
     * @return 文件的访问URL
     */
    public String getFileUrl(String key) {
        key = normalizePath(key);
        return cosConfig.getHost() + "/" + key;
    }

    /**
     * 检查文件是否存在
     *
     * @param key 对象键（文件路径）
     * @return 如果文件存在，则返回true
     */
    public boolean isFileExists(String key) {
        key = normalizePath(key);
        try {
            return cosClient.doesObjectExist(cosConfig.getBucket(), key);
        } catch (Exception e) {
            log.error("Failed to check if file exists in Tencent COS: {}", key, e);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param key 对象键（文件路径）
     * @return 如果删除成功，则返回true
     */
    public boolean deleteFile(String key) {
        key = normalizePath(key);
        try {
            cosClient.deleteObject(cosConfig.getBucket(), key);
            log.info("Deleted file from Tencent COS: {}", key);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from Tencent COS: {}", key, e);
            return false;
        }
    }

    /**
     * 列出目录中的所有文件
     *
     * @param prefix 目录前缀
     * @return 文件名到文件URL的映射
     */
    public Map<String, String> listFiles(String prefix) {
        Map<String, String> files = new HashMap<>();
        prefix = normalizePath(prefix);
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix += "/";
        }

        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(cosConfig.getBucket());
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setDelimiter("/");

            ObjectListing objectListing;
            do {
                objectListing = cosClient.listObjects(listObjectsRequest);
                List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();

                for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                    String key = cosObjectSummary.getKey();
                    if (!key.equals(prefix)) {
                        String fileName = key.substring(prefix.length());
                        if (!fileName.isEmpty()) {
                            files.put(fileName, getFileUrl(key));
                        }
                    }
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        } catch (Exception e) {
            log.error("Failed to list files from Tencent COS: {}", prefix, e);
        }

        return files;
    }

    /**
     * 列出所有目录
     *
     * @param prefix 目录前缀
     * @return 目录名列表
     */
    public List<String> listDirectories(String prefix) {
        List<String> directories = new ArrayList<>();
        prefix = normalizePath(prefix);
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix += "/";
        }

        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(cosConfig.getBucket());
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setDelimiter("/");

            ObjectListing objectListing;
            do {
                objectListing = cosClient.listObjects(listObjectsRequest);
                List<String> commonPrefixes = objectListing.getCommonPrefixes();

                for (String commonPrefix : commonPrefixes) {
                    if (!commonPrefix.equals(prefix)) {
                        // 去除末尾的斜杠
                        String dirName = commonPrefix;
                        if (dirName.endsWith("/")) {
                            dirName = dirName.substring(0, dirName.length() - 1);
                        }
                        // 只获取当前层级的目录名
                        if (dirName.contains("/")) {
                            dirName = dirName.substring(dirName.lastIndexOf("/") + 1);
                        }
                        directories.add(dirName);
                    }
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        } catch (Exception e) {
            log.error("Failed to list directories from Tencent COS: {}", prefix, e);
        }

        return directories;
    }

    /**
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    public String getBucketName() {
        return cosConfig.getBucket();
    }

    /**
     * 获取地域
     *
     * @return 地域
     */
    public String getRegion() {
        return cosConfig.getRegion();
    }

    /**
     * 获取访问域名
     *
     * @return 访问域名
     */
    public String getHost() {
        return cosConfig.getHost();
    }

    /**
     * 标准化路径
     * 去除开头的斜杠，确保路径格式正确
     *
     * @param path 原始路径
     * @return 标准化后的路径
     */
    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        // 去除开头的斜杠
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
