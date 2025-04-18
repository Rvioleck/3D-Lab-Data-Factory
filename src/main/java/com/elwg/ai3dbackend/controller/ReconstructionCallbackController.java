package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.model.dto.callback.StatusUpdateRequest;
import com.elwg.ai3dbackend.model.entity.Model;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;
import com.elwg.ai3dbackend.service.EventStreamService;
import com.elwg.ai3dbackend.service.FileStorageService;
import com.elwg.ai3dbackend.service.ModelService;
import com.elwg.ai3dbackend.service.ReconstructionTaskService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 3D重建回调控制器
 * <p>
 * 提供接收Python服务回调的API接口，包括接收处理结果和状态更新
 * </p>
 */
@RestController
@RequestMapping("/api/reconstruction/callback")
@Api(tags = "3D重建回调接口", description = "接收Python服务的回调，包括处理结果和状态更新")
@Slf4j
public class ReconstructionCallbackController {

    // 旧版本的ModelFileService已弃用
    // @Resource
    // private ModelFileService modelFileService;

    @Resource
    private ReconstructionTaskService reconstructionTaskService;

    @Resource
    private EventStreamService eventStreamService;

    @Resource
    private FileStorageService fileStorageService;

    @Autowired(required = false)
    private ModelService modelService;

    /**
     * 接收结果部分
     * <p>
     * 接收Python服务发送的处理结果部分，如pixel_images.png, xyz_images.png等
     * </p>
     *
     * @param taskId 任务ID
     * @param name 结果部分名称
     * @param file 文件数据
     * @return 处理结果
     */
    @PostMapping("/result/{taskId}")
    @ApiOperation(value = "接收结果部分", notes = "接收Python服务发送的处理结果部分")
    public BaseResponse<Map<String, Object>> receiveResultPart(
            @PathVariable String taskId,
            @RequestParam("name") String name,
            @RequestParam(value = "content_type", defaultValue = "application/octet-stream") String contentType,
            @RequestParam("file") MultipartFile file) {

        log.info("Received result part: {} for task: {}, content type: {}, size: {} bytes",
                name, taskId, contentType, file.getSize());

        try {
            // 检查任务是否存在
            ReconstructionTask task = reconstructionTaskService.getTaskByTaskId(taskId);
            if (task == null) {
                log.warn("Task not found: {}", taskId);
                return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
            }

            // 处理结果部分
            byte[] fileData = file.getBytes();

            // 保存文件到存储服务
            String filePath = "reconstruction/" + taskId + "/" + name;
            fileStorageService.saveFile(filePath, fileData);
            String fileUrl = fileStorageService.getFileUrl(filePath);

            // 更新任务记录
            String fileType = getFileType(name);
            reconstructionTaskService.updateTaskResultFile(taskId, fileType, fileUrl);

            // 发送SSE事件
            eventStreamService.sendResultEvent(taskId, name, fileUrl);

            // 旧版本的ModelFileService已弃用
            // try {
            //     modelFileService.processResultPart(taskId, name, contentType, fileData);
            // } catch (Exception e) {
            //     log.warn("Failed to process result part with old ModelFileService: {}", e.getMessage());
            // }

            // 返回处理结果
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("task_id", taskId);
            result.put("name", name);
            result.put("url", fileUrl);

            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("Failed to process result part", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理结果部分失败：" + e.getMessage());
        }
    }

    /**
     * 接收状态更新
     * <p>
     * 接收Python服务发送的任务状态更新
     * </p>
     *
     * @param request 状态更新请求
     * @return 处理结果
     */
    @PostMapping("/status")
    @ApiOperation(value = "接收状态更新", notes = "接收Python服务发送的任务状态更新")
    public BaseResponse<Map<String, Object>> receiveStatusUpdate(@RequestBody StatusUpdateRequest request) {
        log.info("Received status update for task: {}, status: {}", request.getTaskId(), request.getStatus());

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskByTaskId(request.getTaskId());
        if (task == null) {
            log.warn("Task not found: {}", request.getTaskId());
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 将Python服务的状态映射到我们的状态
        String status;
        switch (request.getStatus()) {
            case "processing":
                status = "PROCESSING";
                break;
            case "completed":
                status = "COMPLETED";
                break;
            case "failed":
                status = "FAILED";
                break;
            default:
                status = request.getStatus().toUpperCase();
        }

        // 更新任务状态
        reconstructionTaskService.updateTaskStatus(request.getTaskId(), status, request.getError());

        // 发送SSE事件
        eventStreamService.sendStatusEvent(request.getTaskId(), status, request.getError());

        // 如果任务完成，创建Model记录
        if ("COMPLETED".equals(status) && modelService != null) {
            try {
                createModelFromTask(task);
            } catch (Exception e) {
                log.error("Failed to create model from task: {}", request.getTaskId(), e);
            }
        }

        // 旧版本的ModelFileService已弃用
        // try {
        //     if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
        //         modelFileService.updateTaskStatus(request.getTaskId(), request.getStatus().toLowerCase());
        //     }
        // } catch (Exception e) {
        //     log.warn("Failed to update task status with old ModelFileService: {}", e.getMessage());
        // }

        // 返回处理结果
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("task_id", request.getTaskId());
        result.put("message", "状态已更新");

        return ResultUtils.success(result);
    }

    /**
     * 根据文件名获取文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        if (fileName.contains("pixel_images") || fileName.equals("pixel_images.png")) {
            return "pixel_images";
        } else if (fileName.contains("xyz_images") || fileName.equals("xyz_images.png")) {
            return "xyz_images";
        } else if (fileName.contains("output3d") || fileName.equals("output3d.zip")) {
            return "output_zip";
        } else {
            return "other";
        }
    }

    /**
     * 从任务创建模型记录
     *
     * @param task 重建任务
     */
    private void createModelFromTask(ReconstructionTask task) {
        // 检查必要的URL是否存在
        if (task.getOutputZipUrl() == null) {
            log.warn("Cannot create model: output zip URL is missing for task: {}", task.getTaskId());
            return;
        }

        // 创建模型记录
        Model model = new Model();
        model.setName("3D Model - " + task.getTaskId());
        model.setSourceImageId(task.getSourceImageId());
        model.setTaskId(task.getTaskId());
        model.setStatus("completed");
        model.setUserId(task.getUserId());

        // 设置URL
        model.setPixelImagesUrl(task.getPixelImagesUrl());
        model.setXyzImagesUrl(task.getXyzImagesUrl());

        // 从输出ZIP中提取OBJ、MTL和纹理文件URL
        // 这里简化处理，实际应该解析ZIP文件
        String basePath = task.getOutputZipUrl().substring(0, task.getOutputZipUrl().lastIndexOf("/") + 1);
        model.setObjFileUrl(basePath + "model.obj");
        model.setMtlFileUrl(basePath + "model.mtl");
        model.setTextureImageUrl(basePath + "texture.png");

        // 保存模型记录
        modelService.save(model);

        // 更新任务的结果模型ID
        reconstructionTaskService.updateTaskResultModel(task.getTaskId(), model.getId());

        log.info("Created model {} for task {}", model.getId(), task.getTaskId());
    }
}
