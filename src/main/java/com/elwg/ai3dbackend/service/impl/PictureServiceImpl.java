package com.elwg.ai3dbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.mapper.PictureMapper;
import com.elwg.ai3dbackend.model.entity.Picture;
import com.elwg.ai3dbackend.service.FileStorageService;
import com.elwg.ai3dbackend.service.PictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图片服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final FileStorageService fileStorageService;


    /**
     * 获取所有不重复的图片分类
     *
     * @return 分类列表
     */
    @Override
    public List<String> listCategories() {
        // 查询所有不为空的分类
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Picture::getCategory)
                .isNotNull(Picture::getCategory)
                .ne(Picture::getCategory, "");

        // 获取分类列表
        List<String> categories = list(queryWrapper).stream()
                .map(Picture::getCategory)
                .distinct()
                .collect(Collectors.toList());

        return categories;
    }

    /**
     * 获取所有不重复的图片标签
     *
     * @return 标签列表
     */
    @Override
    public List<String> listTags() {
        // 查询所有不为空的标签
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Picture::getTags)
                .isNotNull(Picture::getTags)
                .ne(Picture::getTags, "");

        // 获取标签列表
        List<String> allTags = new ArrayList<>();
        list(queryWrapper).forEach(picture -> {
            String tags = picture.getTags();
            if (StrUtil.isNotBlank(tags)) {
                // 假设标签以逗号分隔
                String[] tagArray = tags.split(",");
                allTags.addAll(Arrays.asList(tagArray));
            }
        });

        // 去重并返回
        return allTags.stream()
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 检查并清理无效的图片记录
     * @return 清理的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupInvalidRecords() {
        int cleanedCount = 0;
        try {
            // 1. 获取所有数据库记录的URL和ID
            List<Picture> pictures = list(new LambdaQueryWrapper<Picture>()
                    .select(Picture::getId, Picture::getUrl));
            
            if (pictures.isEmpty()) {
                return 0;
            }

            // 2. 批量检查文件是否存在
            List<Long> idsToDelete = pictures.stream()
                    .filter(picture -> {
                        try {
                            String path = fileStorageService.extractPathFromUrl(picture.getUrl());
                            return !fileStorageService.isFileExists(path);
                        } catch (Exception e) {
                            log.error("Error checking picture: id={}", picture.getId(), e);
                            return false;
                        }
                    })
                    .map(Picture::getId)
                    .collect(Collectors.toList());

            // 3. 批量删除无效记录
            if (!idsToDelete.isEmpty()) {
                cleanedCount = idsToDelete.size();
                removeBatchByIds(idsToDelete);
                log.info("Cleaned {} invalid picture records", cleanedCount);
            }

        } catch (Exception e) {
            log.error("Error during cleanup process", e);
        }

        return cleanedCount;
    }

    /**
     * 清理存储中没有对应数据库记录的文件
     * @return 清理的文件数量
     */
    @Override
    public int cleanupOrphanedFiles() {
        int cleanedCount = 0;
        
        try {
            // 1. 获取COS中的所有图片URL (Set A)
            Set<String> cosUrls = new HashSet<>(fileStorageService.listFiles("images/").values());
            if (cosUrls.isEmpty()) {
                log.info("No files found in storage");
                return 0;
            }

            // 2. 获取数据库中的所有URL (Set B)
            Set<String> dbUrls = list(new LambdaQueryWrapper<Picture>()
                    .select(Picture::getUrl))
                    .stream()
                    .map(Picture::getUrl)
                    .collect(Collectors.toSet());

            // 3. 计算差集 A - (A∩B)，得到孤立文件的URL
            cosUrls.removeAll(dbUrls);

            // 4. 删除孤立文件
            for (String fileUrl : cosUrls) {
                try {
                    String path = fileStorageService.extractPathFromUrl(fileUrl);
                    if (fileStorageService.deleteFile(path)) {
                        cleanedCount++;
                        log.info("Cleaned orphaned file: {}", path);
                    }
                } catch (Exception e) {
                    log.error("Error deleting file: {}", fileUrl, e);
                }
            }
            
            log.info("Cleanup completed. Found and deleted {} orphaned files", cleanedCount);
            
        } catch (Exception e) {
            log.error("Error during cleanup process", e);
        }
        
        return cleanedCount;
    }
}
