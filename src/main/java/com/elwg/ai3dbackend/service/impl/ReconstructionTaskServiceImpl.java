package com.elwg.ai3dbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.constant.TaskStatus;
import com.elwg.ai3dbackend.mapper.ReconstructionTaskMapper;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;
import com.elwg.ai3dbackend.service.ReconstructionTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * 3D重建任务服务实现类
 */
@Slf4j
@Service
public class ReconstructionTaskServiceImpl extends ServiceImpl<ReconstructionTaskMapper, ReconstructionTask> implements ReconstructionTaskService {

    /**
     * 创建重建任务
     *
     * @param userId 用户ID
     * @param sourceImageId 源图片ID
     * @param originalImageUrl 原始图片URL
     * @return 创建的任务
     */
    @Override
    @Transactional
    public ReconstructionTask createTask(Long userId, Long sourceImageId, String originalImageUrl) {
        // 生成唯一的任务ID
        String taskId = UUID.randomUUID().toString().replace("-", "");
        return createTask(taskId, userId, sourceImageId, originalImageUrl);
    }

    /**
     * 创建重建任务（指定任务ID）
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param sourceImageId 源图片ID
     * @param originalImageUrl 原始图片URL
     * @return 创建的任务
     */
    @Override
    @Transactional
    public ReconstructionTask createTask(String taskId, Long userId, Long sourceImageId, String originalImageUrl) {
        // 创建任务实体
        ReconstructionTask task = new ReconstructionTask();
        task.setTaskId(taskId);
        task.setStatus(TaskStatus.PENDING);
        task.setSourceImageId(sourceImageId);
        task.setOriginalImageUrl(originalImageUrl);
        task.setUserId(userId);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setIsDelete(0);

        // 保存到数据库
        save(task);
        log.info("Created reconstruction task: {}", taskId);

        return task;
    }

    /**
     * 根据任务ID查询任务
     *
     * @param taskId 任务ID
     * @return 任务实体，如果不存在则返回null
     */
    @Override
    public ReconstructionTask getTaskByTaskId(String taskId) {
        LambdaQueryWrapper<ReconstructionTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReconstructionTask::getTaskId, taskId);
        return getOne(queryWrapper);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的任务
     */
    @Override
    @Transactional
    public ReconstructionTask updateTaskStatus(String taskId, String status, String errorMessage) {
        ReconstructionTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found: {}", taskId);
            return null;
        }

        // 更新状态
        task.setStatus(status);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            task.setErrorMessage(errorMessage);
        }
        task.setUpdateTime(new Date());

        // 如果任务完成或失败，计算处理时间
        if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            long processingTimeMillis = new Date().getTime() - task.getCreateTime().getTime();
            int processingTimeSeconds = (int) (processingTimeMillis / 1000);
            task.setProcessingTime(processingTimeSeconds);
        }

        // 更新到数据库
        updateById(task);
        log.info("Updated task status: {} -> {}", taskId, status);

        return task;
    }

    /**
     * 更新任务结果文件URL
     *
     * @param taskId 任务ID
     * @param fileType 文件类型（pixel_images, xyz_images, output_zip）
     * @param fileUrl 文件URL
     * @return 更新后的任务
     */
    @Override
    @Transactional
    public ReconstructionTask updateTaskResultFile(String taskId, String fileType, String fileUrl) {
        ReconstructionTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found: {}", taskId);
            return null;
        }

        // 根据文件类型更新相应的URL
        switch (fileType) {
            case "pixel_images":
                task.setPixelImagesUrl(fileUrl);
                break;
            case "xyz_images":
                task.setXyzImagesUrl(fileUrl);
                break;
            case "output_zip":
                task.setOutputZipUrl(fileUrl);
                break;
            default:
                log.warn("Unknown file type: {}", fileType);
                return task;
        }

        task.setUpdateTime(new Date());

        // 更新到数据库
        updateById(task);
        log.info("Updated task result file: {} -> {}: {}", taskId, fileType, fileUrl);

        return task;
    }

    /**
     * 更新任务结果模型ID
     *
     * @param taskId 任务ID
     * @param modelId 模型ID
     * @return 更新后的任务
     */
    @Override
    @Transactional
    public ReconstructionTask updateTaskResultModel(String taskId, Long modelId) {
        ReconstructionTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found: {}", taskId);
            return null;
        }

        task.setResultModelId(modelId);
        task.setUpdateTime(new Date());

        // 更新到数据库
        updateById(task);
        log.info("Updated task result model: {} -> {}", taskId, modelId);

        return task;
    }

    /**
     * 更新任务处理时间
     *
     * @param taskId 任务ID
     * @param processingTime 处理时间（秒）
     * @return 更新后的任务
     */
    @Override
    @Transactional
    public ReconstructionTask updateTaskProcessingTime(String taskId, Integer processingTime) {
        ReconstructionTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found: {}", taskId);
            return null;
        }

        task.setProcessingTime(processingTime);
        task.setUpdateTime(new Date());

        // 更新到数据库
        updateById(task);
        log.info("Updated task processing time: {} -> {} seconds", taskId, processingTime);

        return task;
    }

    /**
     * 分页查询用户的任务列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public Page<ReconstructionTask> listUserTasks(Long userId, String status, int current, int pageSize) {
        Page<ReconstructionTask> page = new Page<>(current, pageSize);

        LambdaQueryWrapper<ReconstructionTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReconstructionTask::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ReconstructionTask::getStatus, status);
        }

        queryWrapper.orderByDesc(ReconstructionTask::getCreateTime);

        return page(page, queryWrapper);
    }

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteTask(String taskId) {
        ReconstructionTask task = getTaskByTaskId(taskId);
        if (task == null) {
            log.warn("Task not found: {}", taskId);
            return false;
        }

        // 逻辑删除
        task.setIsDelete(1);
        task.setUpdateTime(new Date());

        // 更新到数据库
        boolean result = updateById(task);
        if (result) {
            log.info("Deleted task: {}", taskId);
        } else {
            log.warn("Failed to delete task: {}", taskId);
        }

        return result;
    }
}
