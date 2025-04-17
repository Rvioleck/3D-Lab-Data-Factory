package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.model.dto.ReconstructionResponse;
import com.elwg.ai3dbackend.service.ModelFileService;
import com.elwg.ai3dbackend.service.StorageService;
import com.elwg.ai3dbackend.service.WebSocketService;
import com.elwg.ai3dbackend.service.WebSocketService.ResultPartCallback;
import com.elwg.ai3dbackend.service.impl.StorageServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 3D重建控制器
 * <p>
 * 提供3D重建相关的API接口，包括上传图片、获取重建结果等功能。
 * </p>
 */
@RestController
@RequestMapping("/reconstruction")
@Api(tags = "3D重建接口", description = "提供3D重建相关的功能，包括上传图片和获取重建结果")
@Slf4j
public class ReconstructionController {

    @Resource
    private WebSocketService webSocketService;

    @Resource
    private ModelFileService modelFileService;

    @Resource
    private StorageServiceFactory storageServiceFactory;

    // 存储正在进行的渐进式任务
    private final ConcurrentHashMap<String, CompletableFuture<String>> progressiveTasks = new ConcurrentHashMap<>();

    /**
     * 上传图片进行3D重建（异步模式）
     * <p>
     * 接收用户上传的图片，发送到WebSocket服务进行3D重建，并返回任务ID。
     * 这是一个异步接口，会立即返回，客户端需要轮询任务状态。
     * </p>
     *
     * @param file 上传的图片文件
     * @return 包含任务ID的响应对象
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片进行3D重建（异步）", notes = "上传图片并异步处理，返回任务ID，需要轮询状态")
    public BaseResponse<ReconstructionResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "上传的文件不能为空");

        // 检查文件类型
        String contentType = file.getContentType();
        ThrowUtils.throwIf(contentType == null || !contentType.startsWith("image/"),
                ErrorCode.PARAMS_ERROR, "只支持上传图片文件");

        try {
            // 生成任务ID
            String taskId = UUID.randomUUID().toString().replace("-", "");

            // 获取图片数据
            byte[] imageData = file.getBytes();

            // 检查WebSocket连接状态
            if (!webSocketService.isConnected()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "WebSocket服务未连接");
            }

            // 创建结果部分回调
            ResultPartCallback callback = new ResultPartCallback() {
                @Override
                public void onResultPart(String name, String contentType, byte[] data) {
                    try {
                        // 处理结果部分
                        ReconstructionResponse response = modelFileService.processResultPart(taskId, name, contentType, data);
                        log.info("Task {} received result part: {}, size: {} bytes", taskId, name, data.length);
                    } catch (IOException e) {
                        log.error("Failed to process result part {} for task {}", name, taskId, e);
                    }
                }

                @Override
                public void onStatusUpdate(String status, String error) {
                    log.info("Task {} status update: {}, error: {}", taskId, status, error);
                }
            };

            // 发送图片数据到WebSocket服务并渐进式处理结果
            CompletableFuture<String> future = webSocketService.sendImageAndProcessResult(imageData, taskId, callback);
            progressiveTasks.put(taskId, future);

            // 当任务完成或失败时清理
            future.whenComplete((result, ex) -> {
                progressiveTasks.remove(taskId);
                if (ex != null) {
                    log.error("Task {} failed", taskId, ex);
                } else {
                    log.info("Task {} completed with status: {}", taskId, result);
                }
            });

            // 构建响应对象
            ReconstructionResponse response = new ReconstructionResponse();
            response.setTaskId(taskId);
            response.setStatus("processing");

            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("Failed to process image", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片处理失败：" + e.getMessage());
        }
    }

    /**
     * 获取任务状态
     * <p>
     * 根据任务ID获取3D重建任务的状态和已接收的结果文件。
     * </p>
     *
     * @param taskId 任务ID
     * @return 包含任务状态和结果文件URL的响应对象
     */
    @GetMapping("/status/{taskId}")
    @ApiOperation(value = "获取任务状态", notes = "根据任务ID获取3D重建任务的状态和已接收的结果文件")
    public BaseResponse<ReconstructionResponse> getTaskStatus(@PathVariable String taskId) {
        // 检查任务ID是否存在
        ThrowUtils.throwIf(taskId == null || taskId.isEmpty(), ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        // 构建响应对象
        ReconstructionResponse response = new ReconstructionResponse();
        response.setTaskId(taskId);

        // 检查任务是否存在
        boolean resultExists = modelFileService.isTaskResultExists(taskId);
        if (!resultExists) {
            response.setStatus("processing");
            return ResultUtils.success(response);
        }

        // 获取任务状态
        String status = modelFileService.getTaskStatus(taskId);
        response.setStatus(status);

        // 获取已接收的文件URL
        if (modelFileService.isTaskCompleted(taskId)) {
            // 如果任务已完成，设置所有文件URL
            response.setPixelImagesUrl(modelFileService.getFileUrl(taskId, "pixel_images.png"));
            response.setXyzImagesUrl(modelFileService.getFileUrl(taskId, "xyz_images.png"));

            // 获取所有文件
            Map<String, String> files = modelFileService.getTaskFiles(taskId);
            for (String fileName : files.keySet()) {
                if (fileName.endsWith(".obj")) {
                    response.setObjFileUrl(modelFileService.getFileUrl(taskId, fileName));
                } else if (fileName.endsWith(".mtl")) {
                    response.setMtlFileUrl(modelFileService.getFileUrl(taskId, fileName));
                } else if ((fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                        && !fileName.equals("pixel_images.png") && !fileName.equals("xyz_images.png")) {
                    response.setTextureImageUrl(modelFileService.getFileUrl(taskId, fileName));
                }
            }

            // 如果没有找到特定文件，使用默认名称
            if (response.getObjFileUrl() == null) {
                response.setObjFileUrl(modelFileService.getFileUrl(taskId, "model.obj"));
            }
            if (response.getMtlFileUrl() == null) {
                response.setMtlFileUrl(modelFileService.getFileUrl(taskId, "model.mtl"));
            }
            if (response.getTextureImageUrl() == null) {
                response.setTextureImageUrl(modelFileService.getFileUrl(taskId, "texture.png"));
            }
        } else {
            // 如果任务未完成，只设置已接收的文件URL
            StorageService storageService = storageServiceFactory.getStorageService();
            if (storageService.isFileExists(getStoragePath(taskId, "pixel_images.png"))) {
                response.setPixelImagesUrl(modelFileService.getFileUrl(taskId, "pixel_images.png"));
            }
            if (storageService.isFileExists(getStoragePath(taskId, "xyz_images.png"))) {
                response.setXyzImagesUrl(modelFileService.getFileUrl(taskId, "xyz_images.png"));
            }
        }

        return ResultUtils.success(response);
    }

    /**
     * 获取文件的存储路径
     *
     * @param taskId   任务ID
     * @param fileName 文件名
     * @return 存储路径
     */
    private String getStoragePath(String taskId, String fileName) {
        return "reconstruction/" + taskId + "/" + fileName;
    }

    /**
     * 同步上传图片进行3D重建
     * <p>
     * 接收用户上传的图片，发送到WebSocket服务进行3D重建，并等待结果返回。
     * 这是一个同步接口，会阻塞直到处理完成或超时。
     * </p>
     *
     * @param file 上传的图片文件
     * @return 包含任务ID和文件URL的响应对象
     */
    @PostMapping("/upload/sync")
    @ApiOperation(value = "同步上传图片进行3D重建", notes = "上传图片并等待处理完成，直接返回文件URL")
    public BaseResponse<ReconstructionResponse> uploadImageSync(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "上传的文件不能为空");

        // 检查文件类型
        String contentType = file.getContentType();
        ThrowUtils.throwIf(contentType == null || !contentType.startsWith("image/"),
                ErrorCode.PARAMS_ERROR, "只支持上传图片文件");

        try {
            // 生成任务ID
            String taskId = UUID.randomUUID().toString().replace("-", "");

            // 获取图片数据
            byte[] imageData = file.getBytes();


            // 创建结果部分回调
            final ReconstructionResponse[] responseHolder = new ReconstructionResponse[1];

            ResultPartCallback callback = new ResultPartCallback() {
                @Override
                public void onResultPart(String name, String contentType, byte[] data) {
                    try {
                        // 处理结果部分
                        ReconstructionResponse response = modelFileService.processResultPart(taskId, name, contentType, data);
                        responseHolder[0] = response;
                        log.info("Task {} received result part: {}, size: {} bytes", taskId, name, data.length);
                    } catch (IOException e) {
                        log.error("Failed to process result part {} for task {}", name, taskId, e);
                    }
                }

                @Override
                public void onStatusUpdate(String status, String error) {
                    log.info("Task {} status update: {}, error: {}", taskId, status, error);
                }
            };

            // 发送图片数据到WebSocket服务并渐进式处理结果
            String finalStatus = webSocketService.sendImageAndProcessResult(imageData, taskId, callback)
                    .get(2, TimeUnit.MINUTES); // 设置2分钟超时

            log.info("Task {} completed synchronously with status: {}", taskId, finalStatus);

            // 获取最终响应
            ReconstructionResponse response = responseHolder[0];
            if (response == null) {
                response = new ReconstructionResponse();
                response.setTaskId(taskId);
                response.setStatus(finalStatus);
            }

            // 设置任务ID和状态
            response.setTaskId(taskId);
            if ("completed".equals(finalStatus)) {
                response.setStatus("success");
            } else if ("failed".equals(finalStatus)) {
                response.setStatus("failed");
            } else {
                response.setStatus(finalStatus);
            }

            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("Failed to process image", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片处理失败：" + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Processing was interrupted", e);
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理被中断");
        } catch (ExecutionException e) {
            log.error("Processing failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理失败：" + e.getCause().getMessage());
        } catch (TimeoutException e) {
            log.error("Processing timed out", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理超时");
        }
    }

    /**
     * 检查WebSocket连接状态
     * <p>
     * 检查与WebSocket服务器的连接状态。
     * </p>
     *
     * @return 包含连接状态的响应对象
     */
    @GetMapping("/connection/status")
    @ApiOperation(value = "检查WebSocket连接状态", notes = "检查与WebSocket服务器的连接状态")
    public BaseResponse<Boolean> checkConnectionStatus() {
        boolean connected = webSocketService.isConnected();
        return ResultUtils.success(connected);
    }

    /**
     * 初始化WebSocket连接
     * <p>
     * 主动建立与WebSocket服务器的连接。
     * </p>
     *
     * @return 包含连接结果的响应对象
     */
    @PostMapping("/connection/init")
    @ApiOperation(value = "初始化WebSocket连接", notes = "主动建立与WebSocket服务器的连接")
    public BaseResponse<Boolean> initConnection() {
        boolean success = webSocketService.initConnection();
        return ResultUtils.success(success);
    }

    /**
     * 重置WebSocket连接
     * <p>
     * 手动重置WebSocket连接状态，关闭现有连接并在下次请求时重新连接。
     * </p>
     *
     * @return 包含重置结果的响应对象
     */
    @PostMapping("/connection/reset")
    @ApiOperation(value = "重置WebSocket连接", notes = "手动重置WebSocket连接状态")
    @AuthCheck(mustRole = "admin") // 仅管理员可访问
    public BaseResponse<Boolean> resetConnection() {
        webSocketService.resetConnection();
        return ResultUtils.success(true);
    }

    /**
     * 获取重建结果文件
     * <p>
     * 根据任务ID和文件名获取3D重建的结果文件。
     * </p>
     *
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 包含结果文件的响应实体
     */
    @GetMapping("/files/{taskId}/{fileName}")
    @ApiOperation(value = "获取重建结果文件", notes = "根据任务ID和文件名获取3D重建的结果文件")
    public ResponseEntity<byte[]> getResultFile(@PathVariable String taskId, @PathVariable String fileName) {
        // 检查任务ID和文件名是否存在
        ThrowUtils.throwIf(taskId == null || taskId.isEmpty(), ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        ThrowUtils.throwIf(fileName == null || fileName.isEmpty(), ErrorCode.PARAMS_ERROR, "文件名不能为空");

        // 检查任务结果是否存在
        if (!modelFileService.isTaskResultExists(taskId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务结果不存在或尚未完成");
        }

        try {
            // 直接使用文件服务获取文件数据
            byte[] fileData = modelFileService.getFileData(taskId, fileName);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();

            // 根据文件类型设置内容类型
            if (fileName.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (fileName.endsWith(".obj")) {
                headers.setContentType(MediaType.parseMediaType("model/obj"));
            } else if (fileName.endsWith(".mtl")) {
                headers.setContentType(MediaType.parseMediaType("model/mtl"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            // 添加CORS头，允许跨域访问
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");

            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileData.length);

            // 返回文件数据
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Failed to read file for task: {}, fileName: {}", taskId, fileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }

    /**
     * 获取文件内容（用于图片预览）
     * <p>
     * 这个接口用于获取文件内容，主要用于图片预览。
     * 它会返回文件的实际内容，而不是下载链接。
     * </p>
     *
     * @param absolutePath 文件的绝对路径
     * @return 包含文件内容的响应实体
     */
    @GetMapping("/preview")
    @ApiOperation(value = "获取文件内容", notes = "根据文件的绝对路径获取文件内容，主要用于图片预览")
    public ResponseEntity<byte[]> getFileContent(@RequestParam("path") String absolutePath) {
        // 检查路径是否存在
        ThrowUtils.throwIf(absolutePath == null || absolutePath.isEmpty(), ErrorCode.PARAMS_ERROR, "文件路径不能为空");

        File file = new File(absolutePath);
        if (!file.exists() || !file.isFile()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        try {
            // 读取文件数据
            byte[] fileData = Files.readAllBytes(file.toPath());

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();

            // 根据文件类型设置内容类型
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (fileName.endsWith(".obj")) {
                // OBJ文件使用纯文本格式，并添加CORS头
                headers.setContentType(MediaType.parseMediaType("model/obj"));
                headers.add("Access-Control-Allow-Origin", "*");
            } else if (fileName.endsWith(".mtl")) {
                // MTL文件使用纯文本格式，并添加CORS头
                headers.setContentType(MediaType.parseMediaType("model/mtl"));
                headers.add("Access-Control-Allow-Origin", "*");
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            // 添加CORS头，允许跨域访问
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");

            headers.setContentLength(fileData.length);

            // 返回文件数据
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Failed to read file: {}", absolutePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }
}
