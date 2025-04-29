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

    /**
     * 检查并清理无效的图片记录
     * @return 清理的记录数量
     */
    int cleanupInvalidRecords();

    /**
     * 清理存储中没有对应数据库记录的文件
     * @return 清理的文件数量
     */
    int cleanupOrphanedFiles();
}
