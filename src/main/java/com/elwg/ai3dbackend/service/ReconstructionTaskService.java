package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;

/**
 * 3D重建任务服务接口
 * <p>
 * 提供3D重建任务的创建、查询、更新等功能
 * </p>
 */
public interface ReconstructionTaskService {

    /**
     * 创建重建任务
     *
     * @param userId 用户ID
     * @param sourceImageId 源图片ID
     * @param originalImageUrl 原始图片URL
     * @return 创建的任务
     */
    ReconstructionTask createTask(Long userId, Long sourceImageId, String originalImageUrl);

    /**
     * 根据任务ID查询任务
     *
     * @param id 任务ID
     * @return 任务实体，如果不存在则返回null
     */
    ReconstructionTask getTaskById(Long id);

    /**
     * 更新任务状态
     *
     * @param id 任务ID
     * @param status 新状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的任务
     */
    ReconstructionTask updateTaskStatus(Long id, String status, String errorMessage);

    /**
     * 更新任务结果模型ID
     *
     * @param id 任务ID
     * @param modelId 模型ID
     * @return 更新后的任务
     */
    ReconstructionTask updateTaskResultModel(Long id, Long modelId);

    /**
     * 更新任务处理时间
     *
     * @param id 任务ID
     * @param processingTime 处理时间（秒）
     * @return 更新后的任务
     */
    ReconstructionTask updateTaskProcessingTime(Long id, Integer processingTime);

    /**
     * 分页查询用户的任务列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<ReconstructionTask> listUserTasks(Long userId, String status, int current, int pageSize);

    /**
     * 删除任务
     *
     * @param id 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(Long id);
}
