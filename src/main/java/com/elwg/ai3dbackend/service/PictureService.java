package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elwg.ai3dbackend.model.entity.Picture;

import java.util.List;

/**
 * 图片服务接口
 * <p>
 * 提供图片的CRUD操作
 * </p>
 */
public interface PictureService extends IService<Picture> {

    /**
     * 根据ID查询图片
     *
     * @param id 图片ID
     * @return 图片实体，如果不存在则返回null
     */
    Picture getPictureById(Long id);

    /**
     * 获取所有不重复的图片分类
     *
     * @return 分类列表
     */
    List<String> listCategories();

    /**
     * 获取所有不重复的图片标签
     *
     * @return 标签列表
     */
    List<String> listTags();
}
