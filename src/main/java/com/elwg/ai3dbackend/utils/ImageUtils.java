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

}
