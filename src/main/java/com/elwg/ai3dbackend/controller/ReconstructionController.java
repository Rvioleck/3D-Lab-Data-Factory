package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.constant.TaskStatus;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;

import com.elwg.ai3dbackend.model.dto.reconstruction.ReconstructionTaskDTO;
import com.elwg.ai3dbackend.model.dto.reconstruction.ReconstructionUploadResponse;
import com.elwg.ai3dbackend.model.entity.Picture;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.service.EventStreamService;
import com.elwg.ai3dbackend.service.FileStorageService;
import com.elwg.ai3dbackend.service.PictureService;
import com.elwg.ai3dbackend.service.ReconstructionHttpService;
import com.elwg.ai3dbackend.service.ReconstructionTaskService;
import com.elwg.ai3dbackend.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 3D重建控制器
 * <p>
 * 提供3D重建相关的API接口，包括上传图片、获取重建结果等功能。
 * </p>
 */
@RestController
@RequestMapping("/api/reconstruction")
@Api(tags = "3D重建接口", description = "提供3D重建相关的功能，包括上传图片和获取重建结果")
@Slf4j
public class ReconstructionController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private ReconstructionHttpService reconstructionHttpService;

    @Resource
    private ReconstructionTaskService reconstructionTaskService;

    @Resource
    private EventStreamService eventStreamService;

    @Resource
    private FileStorageService fileStorageService;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;



    /**
     * 创建3D重建任务
     *
     * @param imageUrl 图片URL或路径
     * @param name 模型名称（可选）
     * @param request HTTP请求
     * @return 任务ID和SSE URL
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建3D重建任务", notes = "使用已上传的图片创建3D重建任务，返回任务ID和SSE URL")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<ReconstructionUploadResponse> createReconstructionTask(
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "name", required = false) String name,
            HttpServletRequest request) {

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        try {
            // 检查图片URL
            if (imageUrl == null || imageUrl.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
            }

            // 生成任务ID
            String taskId = UUID.randomUUID().toString().replace("-", "");

            // 创建重建任务记录
            ReconstructionTask task = reconstructionTaskService.createTask(
                    taskId, loginUser.getId(), null, imageUrl);

            // 构建回调URL
            String callbackUrl = getCallbackUrl(request);

            // 获取图片数据
            byte[] imageData;
            try {
                // 尝试从存储服务中获取图片数据
                // 如果是完整URL，提取路径部分
                String imagePath = imageUrl;
                if (imageUrl.startsWith("http")) {
                    // 假设路径是URL的最后部分，例如 "images/xxx/image.jpg"
                    imagePath = imageUrl.substring(imageUrl.indexOf("/images/"));
                }
                imageData = fileStorageService.getFileData(imagePath);
            } catch (IOException e) {
                log.error("Failed to get image data for URL: {}", imageUrl, e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取图片数据失败：" + e.getMessage());
            }

            // 异步发送图片到Python服务
            CompletableFuture.runAsync(() -> {
                try {
                    // 更新任务状态为处理中
                    reconstructionTaskService.updateTaskStatus(taskId, TaskStatus.PROCESSING, null);

                    // 发送SSE状态更新
                    eventStreamService.sendStatusEvent(taskId, TaskStatus.PROCESSING, null);

                    // 发送图片到Python服务
                    reconstructionHttpService.sendImageForReconstruction(imageData, taskId, callbackUrl)
                            .thenAccept(status -> {
                                log.info("Image sent to Python service for task: {}, status: {}", taskId, status);
                            })
                            .exceptionally(ex -> {
                                log.error("Failed to send image to Python service for task: {}", taskId, ex);
                                // 更新任务状态为失败
                                reconstructionTaskService.updateTaskStatus(taskId, TaskStatus.FAILED, ex.getMessage());
                                // 发送SSE状态更新
                                eventStreamService.sendStatusEvent(taskId, TaskStatus.FAILED, ex.getMessage());
                                return null;
                            });
                } catch (Exception e) {
                    log.error("Failed to process image for task: {}", taskId, e);
                    // 更新任务状态为失败
                    reconstructionTaskService.updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage());
                    // 发送SSE状态更新
                    eventStreamService.sendStatusEvent(taskId, TaskStatus.FAILED, e.getMessage());
                }
            });

            // 构建响应对象
            ReconstructionUploadResponse response = new ReconstructionUploadResponse();
            response.setTaskId(taskId);
            response.setSseUrl(contextPath + "/api/reconstruction/events/" + taskId);

            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("Failed to create reconstruction task", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建重建任务失败：" + e.getMessage());
        }
    }

    /**
     * SSE事件流
     *
     * @param taskId 任务ID
     * @return SSE发射器
     */
    @GetMapping("/events/{taskId}")
    @ApiOperation(value = "SSE事件流", notes = "建立SSE连接，接收任务状态和结果更新")
    public SseEmitter events(@PathVariable String taskId) {
        log.info("Creating SSE connection for task: {}", taskId);

        // 创建SSE发射器
        SseEmitter emitter = eventStreamService.createEmitter(taskId);

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskByTaskId(taskId);
        if (task == null) {
            // 发送错误事件
            eventStreamService.sendStatusEvent(taskId, TaskStatus.FAILED, "Task not found");
            eventStreamService.completeEmitter(taskId);
            return emitter;
        }

        // 如果任务已经有状态，立即发送
        eventStreamService.sendStatusEvent(taskId, task.getStatus(), task.getErrorMessage());

        // 如果任务已经有结果文件，立即发送
        if (task.getPixelImagesUrl() != null) {
            eventStreamService.sendResultEvent(taskId, "pixel_images.png", task.getPixelImagesUrl());
        }
        if (task.getXyzImagesUrl() != null) {
            eventStreamService.sendResultEvent(taskId, "xyz_images.png", task.getXyzImagesUrl());
        }
        if (task.getOutputZipUrl() != null) {
            eventStreamService.sendResultEvent(taskId, "output3d.zip", task.getOutputZipUrl());
        }

        // 如果任务已经完成或失败，完成SSE连接
        if (TaskStatus.COMPLETED.equals(task.getStatus()) || TaskStatus.FAILED.equals(task.getStatus())) {
            eventStreamService.completeEmitter(taskId);
        }

        return emitter;
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态和结果
     */
    @GetMapping("/status/{taskId}")
    @ApiOperation(value = "获取任务状态", notes = "获取指定任务的状态和结果")
    public BaseResponse<ReconstructionTaskDTO> getTaskStatus(@PathVariable String taskId) {
        log.info("Getting status for task: {}", taskId);

        // 查询任务
        ReconstructionTask task = reconstructionTaskService.getTaskByTaskId(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 转换为DTO
        ReconstructionTaskDTO taskDTO = new ReconstructionTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);

        return ResultUtils.success(taskDTO);
    }

    /**
     * 获取任务列表
     *
     * @param status 任务状态筛选（可选）
     * @param current 当前页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @param request HTTP请求
     * @return 任务列表
     */
    @GetMapping("/tasks")
    @ApiOperation(value = "获取任务列表", notes = "分页获取当前用户的任务列表")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Map<String, Object>> getTasks(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "current", defaultValue = "1") int current,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 查询任务列表
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReconstructionTask> taskPage =
                reconstructionTaskService.listUserTasks(loginUser.getId(), status, current, pageSize);

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("records", taskPage.getRecords());
        result.put("total", taskPage.getTotal());
        result.put("size", taskPage.getSize());
        result.put("current", taskPage.getCurrent());
        result.put("pages", taskPage.getPages());

        return ResultUtils.success(result);
    }

    /**
     * 获取回调URL
     *
     * @param request HTTP请求
     * @return 回调URL
     */
    private String getCallbackUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();

        // 构建回调URL
        return scheme + "://" + serverName + ":" + serverPort + contextPath + "/api/reconstruction/callback";
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

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskByTaskId(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 检查任务是否完成
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务结果不存在或尚未完成");
        }

        try {
            // 根据文件名构建存储路径
            String filePath = "reconstruction/" + taskId + "/" + fileName;

            // 检查文件是否存在
            if (!fileStorageService.isFileExists(filePath)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
            }

            // 获取文件数据
            byte[] fileData = fileStorageService.getFileData(filePath);

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
