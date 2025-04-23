package com.elwg.ai3dbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.mapper.ModelMapper;
import com.elwg.ai3dbackend.model.entity.Model;
import com.elwg.ai3dbackend.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 3D模型服务实现类
 */
@Slf4j
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    /**
     * 根据任务ID查询模型
     *
     * @param taskId 任务ID
     * @return 模型实体，如果不存在则返回null
     */
    @Override
    public Model getModelByTaskId(Long taskId) {
        LambdaQueryWrapper<Model> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Model::getTaskId, taskId.toString());
        return getOne(queryWrapper);
    }

    /**
     * 根据源图片ID查询模型
     *
     * @param sourceImageId 源图片ID
     * @return 模型实体，如果不存在则返回null
     */
    @Override
    public Model getModelBySourceImageId(Long sourceImageId) {
        LambdaQueryWrapper<Model> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Model::getSourceImageId, sourceImageId);
        return getOne(queryWrapper);
    }
}
