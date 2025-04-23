package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.constant.TaskStatus;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.model.dto.callback.StatusUpdateRequest;
import com.elwg.ai3dbackend.model.entity.Model;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;
import com.elwg.ai3dbackend.service.EventStreamService;
import com.elwg.ai3dbackend.service.FileStorageService;
import com.elwg.ai3dbackend.service.ModelService;
import com.elwg.ai3dbackend.service.ReconstructionTaskService;
import com.elwg.ai3dbackend.utils.ZipUtils;

import java.util.concurrent.CompletableFuture;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
@RequestMapping("/reconstruction/callback")
@Api(tags = "3D重建回调接口", description = "接收Python服务的回调，包括处理结果和状态更新")
@Slf4j
public class ReconstructionCallbackController {

    @Resource
    private ReconstructionTaskService reconstructionTaskService;

    @Resource
    private EventStreamService eventStreamService;

    @Resource
    private FileStorageService fileStorageService;

    @Autowired
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
            @PathVariable Long taskId,
            @RequestParam("name") String name,
            @RequestParam(value = "content_type", defaultValue = "application/octet-stream") String contentType,
            @RequestParam("file") MultipartFile file) {

        log.info("Received result part: {} for task: {}, content type: {}, size: {} bytes",
                name, taskId, contentType, file.getSize());

        try {
            // 检查参数
            if (taskId == null || taskId <= 0) {
                log.error("Invalid task ID: {}", taskId);
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "任务ID不合法");
            }
            if (name == null || name.isEmpty()) {
                log.error("Invalid file name for task: {}", taskId);
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件名不能为空");
            }
            if (file == null || file.isEmpty()) {
                log.error("Empty file for task: {}, name: {}", taskId, name);
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件不能为空");
            }

            // 检查任务是否存在
            ReconstructionTask task = reconstructionTaskService.getTaskById(taskId);
            if (task == null) {
                log.warn("Task not found: {}", taskId);
                return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
            }

            // 处理结果部分
            byte[] fileData = file.getBytes();
            log.info("Read {} bytes from file for task: {}, name: {}", fileData.length, taskId, name);

            try {
                // 获取文件类型
                String fileType = getFileType(name);
                log.info("File type: {}", fileType);

                // 如果是ZIP文件，不保存到COS，直接处理
                if (fileType.equals("output_zip")) {
                    log.info("Processing ZIP file directly without saving to COS: {}", name);

                    // 发送SSE事件通知收到ZIP文件
                    log.info("Sending SSE event for ZIP file: {}", name);
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("fileType", fileType);
                    boolean eventSent = eventStreamService.sendEvent(taskId.toString(), "file_received", eventData);
                    if (eventSent) {
                        log.info("SSE event sent successfully for ZIP file: {}", name);
                    } else {
                        log.warn("Failed to send SSE event for ZIP file: {}, no active connection", name);
                    }
                } else {
                    // 其他文件保存到存储服务
                    String filePath = "reconstruction/" + taskId + "/" + name;
                    log.info("Saving file to storage: {}", filePath);
                    fileStorageService.saveFile(filePath, fileData);
                    String fileUrl = fileStorageService.getFileUrl(filePath);
                    log.info("File saved, URL: {}", fileUrl);

                    // 发送SSE事件
                    log.info("Sending SSE event for file: {}", name);
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("fileType", fileType);
                    eventData.put("fileUrl", fileUrl);
                    boolean eventSent = eventStreamService.sendEvent(taskId.toString(), "file_received", eventData);
                    if (eventSent) {
                        log.info("SSE event sent successfully for file: {}", name);
                    } else {
                        log.warn("Failed to send SSE event for file: {}, no active connection", name);
                    }
                }

                // 定义文件URL变量
                String fileUrl = null;

                // 如果不是ZIP文件，获取文件URL
                if (!fileType.equals("output_zip")) {
                    fileUrl = fileStorageService.getFileUrl("reconstruction/" + taskId + "/" + name);
                }

                // 处理不同类型的文件
                if (fileType.equals("pixel_images")) {
                    // 第一个回调：创建Model记录
                    handlePixelImagesCallback(task, fileUrl);
                } else if (fileType.equals("xyz_images")) {
                    // 第二个回调：更新Model记录
                    handleXyzImagesCallback(task, fileUrl);
                } else if (fileType.equals("output_zip")) {
                    // 最后一个回调：处理ZIP文件并更新Model记录
                    // 对于ZIP文件，我们不存储到COS，直接处理内容
                    handleOutputZipCallback(task, fileData);

                    // 返回成功响应
                    Map<String, Object> result = new HashMap<>();
                    result.put("taskId", taskId);
                    result.put("fileType", fileType);
                    return ResultUtils.success(result);
                }

                // 返回成功响应
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", taskId);
                result.put("fileType", fileType);
                if (fileUrl != null) {
                    result.put("fileUrl", fileUrl);
                }
                return ResultUtils.success(result);
            } catch (Exception e) {
                log.error("Failed to process file for task: {}, name: {}, error: {}", taskId, name, e.getMessage(), e);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "处理文件失败: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Failed to read file data for task: {}, name: {}, error: {}", taskId, name, e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "读取文件数据失败: " + e.getMessage());
        }
    }

    /**
     * 处理像素图像回调
     *
     * @param task 重建任务
     * @param fileUrl 文件URL
     */
    private void handlePixelImagesCallback(ReconstructionTask task, String fileUrl) {
        log.info("Handling pixel_images callback for task: {}", task.getId());

        // 遵循"一个回调，一个存COS，一个写数据库，一个回SSE消息"的模式
        // COS存储已经在调用此方法前完成

        // 1. 写入数据库 - 使用统一的方法更新模型
        updateModelWithFile(task, "pixel_images", fileUrl);

        // 2. 发送SSE事件通知前端像素图像已完成
        eventStreamService.sendResultEvent(task.getId().toString(), "pixel_images.png", fileUrl);
    }

    /**
     * 处理XYZ图像回调
     *
     * @param task 重建任务
     * @param fileUrl 文件URL
     */
    private void handleXyzImagesCallback(ReconstructionTask task, String fileUrl) {
        log.info("Handling xyz_images callback for task: {}", task.getId());

        // 遵循"一个回调，一个存COS，一个写数据库，一个回SSE消息"的模式
        // COS存储已经在调用此方法前完成

        // 1. 写入数据库 - 使用统一的方法更新模型
        updateModelWithFile(task, "xyz_images", fileUrl);

        // 2. 发送SSE事件通知前端XYZ图像已完成
        eventStreamService.sendResultEvent(task.getId().toString(), "xyz_images.png", fileUrl);
    }

    /**
     * 处理输出ZIP回调
     *
     * @param task 重建任务
     * @param zipData ZIP文件数据
     */
    private void handleOutputZipCallback(ReconstructionTask task, byte[] zipData) {
        log.info("Handling output_zip callback for task: {}", task.getId());

        try {
            // 1. 解压ZIP文件
            Map<String, byte[]> extractedFiles = ZipUtils.unzip(zipData);
            log.info("Extracted {} files from ZIP for task: {}", extractedFiles.size(), task.getId());

            // 构建基础路径
            String basePath = "reconstruction/" + task.getId() + "/";

            // 分类文件
            byte[] objFileData = null;
            byte[] mtlFileData = null;
            byte[] textureFileData = null;

            // 遍历并分类文件
            for (Map.Entry<String, byte[]> entry : extractedFiles.entrySet()) {
                String fileName = entry.getKey();
                byte[] fileData = entry.getValue();
                String extension = ZipUtils.getFileExtension(fileName);

                // 根据文件扩展名确定文件类型
                if ("obj".equals(extension)) {
                    objFileData = fileData;
                    log.info("Found OBJ file: {}", fileName);
                } else if ("mtl".equals(extension)) {
                    mtlFileData = fileData;
                    log.info("Found MTL file: {}", fileName);
                } else if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension)) {
                    // 排除已有的pixel_images.png和xyz_images.png
                    if (!fileName.contains("pixel_images") && !fileName.contains("xyz_images")) {
                        textureFileData = fileData;
                        log.info("Found texture file: {}", fileName);
                    }
                }
            }

            // 2. 存储所有文件到COS
            String objFileUrl = null;
            String mtlFileUrl = null;
            String textureImageUrl = null;
            String outputZipUrl = null;

            // 存储OBJ文件
            if (objFileData != null) {
                String objFilePath = basePath + "model.obj";
                log.info("Saving OBJ file with standard name: {}", objFilePath);
                fileStorageService.saveFile(objFilePath, objFileData);
                objFileUrl = fileStorageService.getFileUrl(objFilePath);
            }

            // 存储MTL文件
            if (mtlFileData != null) {
                String mtlFilePath = basePath + "model.mtl";
                log.info("Saving MTL file with standard name: {}", mtlFilePath);
                fileStorageService.saveFile(mtlFilePath, mtlFileData);
                mtlFileUrl = fileStorageService.getFileUrl(mtlFilePath);
            }

            // 存储纹理文件
            if (textureFileData != null) {
                String texturePath = basePath + "texture.png";
                log.info("Saving texture file with standard name: {}", texturePath);
                fileStorageService.saveFile(texturePath, textureFileData);
                textureImageUrl = fileStorageService.getFileUrl(texturePath);
            }

            // 存储原始ZIP文件
            String zipFilePath = basePath + "output3d.zip";
            fileStorageService.saveFile(zipFilePath, zipData);
            outputZipUrl = fileStorageService.getFileUrl(zipFilePath);

            // 3. 一次性更新数据库中的所有URL
            // 获取或创建模型记录
            Model model = null;

            // 如果任务有关联的模型ID，尝试获取该模型
            if (task.getResultModelId() != null) {
                model = modelService.getById(task.getResultModelId());
            }

            // 如果没有关联的模型ID或者模型不存在，尝试通过taskId查找
            if (model == null) {
                model = modelService.getModelByTaskId(task.getId());
            }

            // 如果仍然没有找到模型，创建一个新的
            if (model == null) {
                log.info("No model found for task: {}, creating new model", task.getId());
                model = new Model();
                model.setName("3D Model - " + task.getId());
                model.setSourceImageId(task.getSourceImageId());
                model.setTaskId(task.getId().toString());
                model.setStatus("PROCESSING"); // 初始状态为处理中
                model.setUserId(task.getUserId());
                model.setModelFormat("OBJ");

                // 设置创建时间
                Date now = new Date();
                model.setCreateTime(now);
                model.setUpdateTime(now);
            }

            // 更新所有URL字段
            if (objFileUrl != null) {
                model.setObjFileUrl(objFileUrl);
            }
            if (mtlFileUrl != null) {
                model.setMtlFileUrl(mtlFileUrl);
            }
            if (textureImageUrl != null) {
                model.setTextureImageUrl(textureImageUrl);
            }

            // 检查是否已有像素图像和XYZ图像
            String pixelImagesPath = basePath + "pixel_images.png";
            if (fileStorageService.isFileExists(pixelImagesPath) && model.getPixelImagesUrl() == null) {
                model.setPixelImagesUrl(fileStorageService.getFileUrl(pixelImagesPath));
            }

            String xyzImagesPath = basePath + "xyz_images.png";
            if (fileStorageService.isFileExists(xyzImagesPath) && model.getXyzImagesUrl() == null) {
                model.setXyzImagesUrl(fileStorageService.getFileUrl(xyzImagesPath));
            }

            // 更新时间
            model.setUpdateTime(new Date());

            // 保存或更新模型
            boolean success;
            if (model.getId() == null) {
                // 新模型，需要保存
                success = modelService.save(model);
                if (success) {
                    log.info("Created new model for task: {}, model ID: {}", task.getId(), model.getId());
                    // 更新任务的结果模型ID
                    reconstructionTaskService.updateTaskResultModel(task.getId(), model.getId());
                } else {
                    log.error("Failed to create model for task: {}", task.getId());
                }
            } else {
                // 现有模型，需要更新
                success = modelService.updateById(model);
                if (success) {
                    log.info("Updated existing model ID: {} with OBJ/MTL/texture URLs", model.getId());
                } else {
                    log.error("Failed to update model ID: {} for task: {}", model.getId(), task.getId());
                }
            }

            // 4. 发送SSE消息
            // 发送OBJ文件的SSE消息
            if (objFileUrl != null) {
                eventStreamService.sendResultEvent(task.getId().toString(), "model.obj", objFileUrl);
            }

            // 发送MTL文件的SSE消息
            if (mtlFileUrl != null) {
                eventStreamService.sendResultEvent(task.getId().toString(), "model.mtl", mtlFileUrl);
            }

            // 发送纹理文件的SSE消息
            if (textureImageUrl != null) {
                eventStreamService.sendResultEvent(task.getId().toString(), "texture.png", textureImageUrl);
            }

            // 发送ZIP文件的SSE消息
            if (outputZipUrl != null) {
                eventStreamService.sendResultEvent(task.getId().toString(), "output3d.zip", outputZipUrl);
            }

        } catch (Exception e) {
            log.error("Failed to process output ZIP for task: {}", task.getId(), e);
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
        log.info("Received status update for task: {}, status: {}, error: {}",
                request.getTaskId(), request.getStatus(), request.getError());

        // 检查参数
        if (request.getTaskId() == null) {
            log.error("Invalid task ID in status update");
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        }
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            log.error("Invalid status in update for task: {}", request.getTaskId());
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "状态不能为空");
        }

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskById(request.getTaskId());
        if (task == null) {
            log.warn("Task not found: {}", request.getTaskId());
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 将Python服务的状态映射到我们的状态常量
        String status;
        switch (request.getStatus().toLowerCase()) {
            case "processing":
                status = TaskStatus.PROCESSING;
                break;
            case "completed":
                status = TaskStatus.COMPLETED;
                break;
            case "failed":
                status = TaskStatus.FAILED;
                break;
            default:
                log.warn("Unknown status from Python service: {}", request.getStatus());
                status = TaskStatus.FAILED;
                request.setError("未知的任务状态: " + request.getStatus());
        }

        log.info("Updating task status: {} -> {}, error: {}", request.getTaskId(), status, request.getError());

        // 更新任务状态
        reconstructionTaskService.updateTaskStatus(request.getTaskId(), status, request.getError());

        // 发送SSE事件
        log.info("Sending SSE status event for task: {}", request.getTaskId());
        boolean eventSent = eventStreamService.sendStatusEvent(request.getTaskId().toString(), status, request.getError());
        if (eventSent) {
            log.info("SSE status event sent successfully for task: {}", request.getTaskId());
        } else {
            log.warn("Failed to send SSE status event for task: {}, no active connection", request.getTaskId());
        }

        // 如果任务完成或失败，更新模型状态
        if (TaskStatus.COMPLETED.equals(status) || TaskStatus.FAILED.equals(status)) {
            updateModelStatus(task, status);
        }

        // 返回成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", request.getTaskId());
        result.put("status", status);
        return ResultUtils.success(result);
    }

    /**
     * 更新模型状态
     *
     * @param task 重建任务
     * @param status 状态
     */
    private void updateModelStatus(ReconstructionTask task, String status) {
        if (task.getResultModelId() != null) {
            Model model = modelService.getById(task.getResultModelId());
            if (model != null) {
                model.setStatus(status.toLowerCase()); // 模型状态使用小写
                model.setUpdateTime(new Date());
                modelService.updateById(model);
                log.info("Updated model status to {} for task: {}", status, task.getId());
            } else {
                log.warn("Model not found for task: {}, model ID: {}", task.getId(), task.getResultModelId());
            }
        } else {
            log.warn("No model associated with task: {}", task.getId());
        }
    }

    /**
     * 创建新模型
     *
     * @param task 重建任务
     * @param objFileUrl OBJ文件URL
     * @param mtlFileUrl MTL文件URL
     * @param textureImageUrl 纹理图像URL
     * @return 创建的模型
     */
    private Model createModel(ReconstructionTask task, String objFileUrl, String mtlFileUrl, String textureImageUrl) {
        log.info("Creating new model for task: {}", task.getId());

        // 创建新模型
        Model model = new Model();
        model.setName("3D Model - " + task.getId());
        model.setSourceImageId(task.getSourceImageId());
        model.setTaskId(task.getId().toString());
        model.setStatus("PROCESSING"); // 初始状态为处理中
        model.setUserId(task.getUserId());
        model.setModelFormat("OBJ");

        // 获取已存在的像素图像和XYZ图像的URL
        String basePath = "reconstruction/" + task.getId() + "/";
        String pixelImagesPath = basePath + "pixel_images.png";
        String xyzImagesPath = basePath + "xyz_images.png";

        if (fileStorageService.isFileExists(pixelImagesPath)) {
            model.setPixelImagesUrl(fileStorageService.getFileUrl(pixelImagesPath));
        }

        if (fileStorageService.isFileExists(xyzImagesPath)) {
            model.setXyzImagesUrl(fileStorageService.getFileUrl(xyzImagesPath));
        }

        // 设置模型文件URL
        model.setObjFileUrl(objFileUrl != null ? objFileUrl : "pending");
        model.setMtlFileUrl(mtlFileUrl != null ? mtlFileUrl : null); // MTL可以为空
        model.setTextureImageUrl(textureImageUrl != null ? textureImageUrl : null); // 纹理可以为空

        // 设置创建时间
        Date now = new Date();
        model.setCreateTime(now);
        model.setUpdateTime(now);

        // 保存模型
        boolean saved = modelService.save(model);
        if (saved) {
            log.info("Created new model for task: {}, model ID: {}", task.getId(), model.getId());

            // 更新任务的结果模型ID
            reconstructionTaskService.updateTaskResultModel(task.getId(), model.getId());
            return model;
        } else {
            log.error("Failed to create model for task: {}", task.getId());
            return null;
        }
    }

    /**
     * 更新现有模型
     *
     * @param task 重建任务
     * @param objFileUrl OBJ文件URL
     * @param mtlFileUrl MTL文件URL
     * @param textureImageUrl 纹理图像URL
     * @return 更新后的模型
     */
    private Model updateModel(ReconstructionTask task, String objFileUrl, String mtlFileUrl, String textureImageUrl) {
        log.info("Updating model for task: {}", task.getId());

        Model model = modelService.getModelByTaskId(task.getId());
        if (model == null) {
            log.warn("Model not found for task: {}, creating new model", task.getId());
            return createModel(task, objFileUrl, mtlFileUrl, textureImageUrl);
        }

        // 更新模型文件URL
        if (objFileUrl != null) {
            model.setObjFileUrl(objFileUrl);
        }
        if (mtlFileUrl != null) {
            model.setMtlFileUrl(mtlFileUrl);
        }
        if (textureImageUrl != null) {
            model.setTextureImageUrl(textureImageUrl);
        }

        model.setUpdateTime(new Date());
        modelService.updateById(model);
        log.info("Updated model with OBJ/MTL/texture URLs for task: {}", task.getId());

        return model;
    }

    /**
     * 使用指定类型的文件URL更新模型
     *
     * @param task 重建任务
     * @param fileType 文件类型（obj, mtl, texture, zip）
     * @param fileUrl 文件URL
     */
    private void updateModelWithFile(ReconstructionTask task, String fileType, String fileUrl) {
        // 遵循"一个回调，一个存COS，一个写数据库，一个回SSE消息"的模式
        // COS存储已经在调用此方法前完成

        // 首先检查是否已经有与该任务关联的模型记录
        Model model = null;

        // 如果任务有关联的模型ID，尝试获取该模型
        if (task.getResultModelId() != null) {
            model = modelService.getById(task.getResultModelId());
        }

        // 如果没有关联的模型ID或者模型不存在，尝试通过taskId查找
        if (model == null) {
            model = modelService.getModelByTaskId(task.getId());
        }

        // 如果仍然没有找到模型，创建一个新的
        if (model == null) {
            log.info("No model found for task: {}, creating new model", task.getId());
            model = new Model();
            model.setName("3D Model - " + task.getId());
            model.setSourceImageId(task.getSourceImageId());
            model.setTaskId(task.getId().toString());
            model.setStatus("PROCESSING"); // 初始状态为处理中
            model.setUserId(task.getUserId());
            model.setModelFormat("OBJ");

            // 设置objFileUrl的默认值，因为数据库中这个字段是必填的
            model.setObjFileUrl("pending");

            // 设置创建时间
            Date now = new Date();
            model.setCreateTime(now);
            model.setUpdateTime(now);
        }

        // 根据文件类型更新相应的URL字段
        switch (fileType) {
            case "obj":
                model.setObjFileUrl(fileUrl);
                break;
            case "mtl":
                model.setMtlFileUrl(fileUrl);
                break;
            case "texture":
                model.setTextureImageUrl(fileUrl);
                break;
            case "pixel_images":
                model.setPixelImagesUrl(fileUrl);
                break;
            case "xyz_images":
                model.setXyzImagesUrl(fileUrl);
                break;
            case "zip":
                // 如果模型有ZIP URL字段，可以在这里设置
                break;
        }

        // 更新时间
        model.setUpdateTime(new Date());

        // 保存或更新模型
        boolean success;
        if (model.getId() == null) {
            // 新模型，需要保存
            success = modelService.save(model);
            if (success) {
                log.info("Created new model for task: {}, model ID: {}", task.getId(), model.getId());
                // 更新任务的结果模型ID
                reconstructionTaskService.updateTaskResultModel(task.getId(), model.getId());
            } else {
                log.error("Failed to create model for task: {}", task.getId());
            }
        } else {
            // 现有模型，需要更新
            success = modelService.updateById(model);
            if (success) {
                log.info("Updated existing model ID: {} with {} URL: {}", model.getId(), fileType, fileUrl);
            } else {
                log.error("Failed to update model ID: {} for task: {}", model.getId(), task.getId());
            }
        }
    }

    /**
     * 获取文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null) {
            return "unknown";
        }

        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.contains("pixel_images") || lowerFileName.equals("pixel_images.png")) {
            return "pixel_images";
        } else if (lowerFileName.contains("xyz_images") || lowerFileName.equals("xyz_images.png")) {
            return "xyz_images";
        } else if (lowerFileName.contains("output3d") || lowerFileName.equals("output3d.zip")) {
            return "output_zip";
        } else if (lowerFileName.endsWith(".obj")) {
            return "obj_file";
        } else if (lowerFileName.endsWith(".mtl")) {
            return "mtl_file";
        } else if (lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "texture_file";
        } else {
            log.info("Unknown file type for file: {}", fileName);
            return "other";
        }
    }
}
