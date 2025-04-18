package com.elwg.ai3dbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.mapper.PictureMapper;
import com.elwg.ai3dbackend.model.entity.Picture;
import com.elwg.ai3dbackend.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片服务实现类
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    /**
     * 根据ID查询图片
     *
     * @param id 图片ID
     * @return 图片实体，如果不存在则返回null
     */
    @Override
    public Picture getPictureById(Long id) {
        return getById(id);
    }

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
}
