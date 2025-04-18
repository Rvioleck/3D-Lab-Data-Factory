package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elwg.ai3dbackend.model.entity.Model;

/**
 * 3D模型服务接口
 * <p>
 * 提供3D模型的CRUD操作
 * </p>
 */
public interface ModelService extends IService<Model> {

    /**
     * 根据任务ID查询模型
     *
     * @param taskId 任务ID
     * @return 模型实体，如果不存在则返回null
     */
    Model getModelByTaskId(String taskId);

    /**
     * 根据源图片ID查询模型
     *
     * @param sourceImageId 源图片ID
     * @return 模型实体，如果不存在则返回null
     */
    Model getModelBySourceImageId(Long sourceImageId);
}
