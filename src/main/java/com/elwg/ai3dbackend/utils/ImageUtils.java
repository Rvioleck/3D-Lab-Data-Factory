package com.elwg.ai3dbackend.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片工具类
 */
@Slf4j
public class ImageUtils {

    /**
     * 获取图片信息
     *
     * @param inputStream 图片输入流
     * @return 图片信息，包括宽度、高度等
     */
    public static Map<String, Object> getImageInfo(InputStream inputStream) {
        Map<String, Object> imageInfo = new HashMap<>();
        
        try {
            // 读取图片
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                // 获取图片宽度和高度
                int width = image.getWidth();
                int height = image.getHeight();
                
                // 设置图片信息
                imageInfo.put("width", width);
                imageInfo.put("height", height);
                imageInfo.put("scale", (double) width / height);
            }
        } catch (IOException e) {
            log.error("获取图片信息失败", e);
        }
        
        return imageInfo;
    }
    
    /**
     * 从URL中提取文件路径
     *
     * @param url 文件URL
     * @return 文件路径
     */
    public static String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 假设URL格式为：https://example.com/path/to/file.jpg
        // 我们需要提取 path/to/file.jpg 部分
        
        // 移除协议和域名部分
        int protocolEnd = url.indexOf("://");
        if (protocolEnd != -1) {
            url = url.substring(protocolEnd + 3);
        }
        
        // 移除域名部分
        int domainEnd = url.indexOf("/");
        if (domainEnd != -1) {
            url = url.substring(domainEnd + 1);
        }
        
        return url;
    }
}
