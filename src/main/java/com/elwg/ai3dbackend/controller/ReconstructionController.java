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
import com.elwg.ai3dbackend.model.entity.Model;
import com.elwg.ai3dbackend.model.entity.Picture;
import com.elwg.ai3dbackend.model.entity.ReconstructionTask;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 3D重建控制器
 * 提供3D重建相关的API接口，包括上传图片、获取重建结果等功能。
 *
 * @author fyz
 */
@RestController
@RequestMapping("/reconstruction")
@Tag(name = "3D重建接口", description = "提供3D重建相关的功能，包括上传图片和获取重建结果")
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

    @Resource
    private ModelService modelService;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;


    /**
     * 创建3D重建任务
     *
     * @param imageId 图片ID
     * @param name    模型名称（可选）
     * @param request HTTP请求
     * @return 任务ID和SSE URL
     */
    @PostMapping("/create")
    @Operation(summary = "创建3D重建任务", description = "使用已上传的图片创建3D重建任务，返回任务ID和SSE URL")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<ReconstructionUploadResponse> createReconstructionTask(
            @RequestParam("imageId") Long imageId,
            @RequestParam(value = "name", required = false) String name,
            HttpServletRequest request) {

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        try {
            // 检查图片ID
            ThrowUtils.throwIf(imageId == null || imageId <= 0, ErrorCode.PARAMS_ERROR, "图片ID不合法");
            // 查询图片信息
            Picture picture = pictureService.getById(imageId);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 获取图片URL
            String imageUrl = picture.getUrl();
            ThrowUtils.throwIf(imageUrl == null || imageUrl.isEmpty(), ErrorCode.SYSTEM_ERROR, "图片URL为空");
            // 创建重建任务记录
            ReconstructionTask task = reconstructionTaskService.createTask(
                    loginUser.getId(), imageId, imageUrl);
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

            // 异步处理重建任务
            processReconstructionTaskAsync(task.getId().toString(), imageData, callbackUrl);

            // 构建响应对象
            ReconstructionUploadResponse response = new ReconstructionUploadResponse();
            response.setTaskId(task.getId());
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("Failed to create reconstruction task", e);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "创建重建任务失败：" + e.getMessage());
            return null; // 这行代码不会执行，因为ThrowUtils会抛出异常
        }
    }

    /**
     * SSE事件流
     *
     * @param id 任务ID
     * @return SSE发射器
     */
    @GetMapping("/events/{id}")
    @Operation(summary = "SSE事件流", description = "建立SSE连接，接收任务状态和结果更新")
    public SseEmitter events(@PathVariable Long id) {
        log.info("Creating SSE connection for task: {}", id);

        // 创建SSE发射器
        String taskIdStr = id.toString();
        SseEmitter emitter = eventStreamService.createEmitter(taskIdStr);

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskById(id);
        if (task == null) {
            // 发送错误事件
            eventStreamService.sendStatusEvent(taskIdStr, TaskStatus.FAILED, "Task not found");
            eventStreamService.completeEmitter(taskIdStr);
            return emitter;
        }

        // 如果任务已经有状态，立即发送
        eventStreamService.sendStatusEvent(taskIdStr, task.getStatus(), task.getErrorMessage());

        // 如果任务已经有结果文件，立即发送
        if (task.getResultModelId() != null) {
            Model model = modelService.getById(task.getResultModelId());
            if (model != null) {
                // 发送像素图像
                if (model.getPixelImagesUrl() != null) {
                    eventStreamService.sendResultEvent(taskIdStr, "pixel_images.png", model.getPixelImagesUrl());
                }
                // 发送XYZ图像
                if (model.getXyzImagesUrl() != null) {
                    eventStreamService.sendResultEvent(taskIdStr, "xyz_images.png", model.getXyzImagesUrl());
                }

                // 检查是否有输出ZIP包
                String outputZipPath = "reconstruction/" + id + "/output3d.zip";
                if (fileStorageService.isFileExists(outputZipPath)) {
                    String outputZipUrl = fileStorageService.getFileUrl(outputZipPath);
                    eventStreamService.sendResultEvent(taskIdStr, "output3d.zip", outputZipUrl);
                }
            }
        }

        // 如果任务已经完成或失败，延迟关闭连接，确保客户端有足够时间接收所有事件
        if (TaskStatus.COMPLETED.equals(task.getStatus()) || TaskStatus.FAILED.equals(task.getStatus())) {
            // 使用延迟关闭，确保客户端有时间接收所有事件
            new Thread(() -> {
                try {
                    // 等待几秒钟，确保客户端有时间接收所有事件
                    Thread.sleep(5000);
                    // 完成SSE连接
                    eventStreamService.completeEmitter(taskIdStr);
                    log.info("Completed SSE connection for completed/failed task: {}", id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted while waiting to complete SSE connection for task: {}", id);
                }
            }).start();
        }

        return emitter;
    }

    /**
     * 获取任务状态
     *
     * @param id 任务ID
     * @return 任务状态和结果
     */
    @GetMapping("/status/{id}")
    @Operation(summary = "获取任务状态", description = "获取指定任务的状态和结果")
    public BaseResponse<ReconstructionTaskDTO> getTaskStatus(@PathVariable Long id) {
        log.info("Getting status for task: {}", id);

        // 查询任务
        ReconstructionTask task = reconstructionTaskService.getTaskById(id);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");

        // 转换为DTO
        ReconstructionTaskDTO taskDTO = new ReconstructionTaskDTO();
        BeanUtils.copyProperties(task, taskDTO);

        // 如果有关联的模型，从模型中获取URL字段
        if (task.getResultModelId() != null) {
            Model model = modelService.getById(task.getResultModelId());
            if (model != null) {
                // 设置URL字段
                taskDTO.setPixelImagesUrl(model.getPixelImagesUrl());
                taskDTO.setXyzImagesUrl(model.getXyzImagesUrl());
                String outputZipPath = "reconstruction/" + id + "/output3d.zip";
                if (fileStorageService.isFileExists(outputZipPath)) {
                    taskDTO.setOutputZipUrl(fileStorageService.getFileUrl(outputZipPath));
                }
            }
        }

        return ResultUtils.success(taskDTO);
    }

    /**
     * 获取任务列表
     *
     * @param status   任务状态筛选（可选）
     * @param current  当前页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @param request  HTTP请求
     * @return 任务列表
     */
    @GetMapping("/tasks")
    @Operation(summary = "获取任务列表", description = "分页获取当前用户的任务列表")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Map<String, Object>> getTasks(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "current", defaultValue = "1") int current,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

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
     * 获取重建结果文件URL
     * <p>
     * 根据任务ID和文件名获取3D重建的结果文件URL，重定向到实际文件地址。
     * </p>
     *
     * @param id       任务ID
     * @param fileName 文件名
     * @return 重定向到文件URL的响应
     */
    @GetMapping("/files/{id}/{fileName}")
    @Operation(summary = "获取重建结果文件URL", description = "根据任务ID和文件名获取3D重建的结果文件URL")
    public ResponseEntity<Void> getResultFile(@PathVariable Long id, @PathVariable String fileName) {
        // 检查任务ID和文件名是否存在
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "任务ID不合法");
        ThrowUtils.throwIf(fileName == null || fileName.isEmpty(), ErrorCode.PARAMS_ERROR, "文件名不能为空");

        log.info("Getting result file URL: {} for task: {}", fileName, id);

        // 检查任务是否存在
        ReconstructionTask task = reconstructionTaskService.getTaskById(id);
        if (task == null) {
            log.warn("Task not found: {}", id);
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 检查任务是否完成
        if (!TaskStatus.COMPLETED.equals(task.getStatus())) {
            log.warn("Task not completed: {}, status: {}", id, task.getStatus());
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "任务结果不存在或尚未完成");
        }

        // 根据文件名获取对应的URL
        String fileUrl = null;

        // 检查是否有关联的模型
        if (task.getResultModelId() != null) {
            Model model = modelService.getById(task.getResultModelId());
            if (model != null) {
                if ("pixel_images.png".equals(fileName) && model.getPixelImagesUrl() != null) {
                    fileUrl = model.getPixelImagesUrl();
                    log.info("Using model pixelImagesUrl: {}", fileUrl);
                } else if ("xyz_images.png".equals(fileName) && model.getXyzImagesUrl() != null) {
                    fileUrl = model.getXyzImagesUrl();
                    log.info("Using model xyzImagesUrl: {}", fileUrl);
                } else if ("output3d.zip".equals(fileName)) {
                    // 对于ZIP文件，使用标准路径
                    String outputZipPath = "reconstruction/" + id + "/output3d.zip";
                    if (fileStorageService.isFileExists(outputZipPath)) {
                        fileUrl = fileStorageService.getFileUrl(outputZipPath);
                        log.info("Using standard path for output3d.zip: {}", fileUrl);
                    }
                } else if ("model.obj".equals(fileName) || "model.mtl".equals(fileName) || "texture.png".equals(fileName)) {
                    if ("model.obj".equals(fileName) && model.getObjFileUrl() != null) {
                        fileUrl = model.getObjFileUrl();
                        log.info("Using model objFileUrl: {}", fileUrl);
                    } else if ("model.mtl".equals(fileName) && model.getMtlFileUrl() != null) {
                        fileUrl = model.getMtlFileUrl();
                        log.info("Using model mtlFileUrl: {}", fileUrl);
                    } else if ("texture.png".equals(fileName) && model.getTextureImageUrl() != null) {
                        fileUrl = model.getTextureImageUrl();
                        log.info("Using model textureImageUrl: {}", fileUrl);
                    }
                }
            }
        }

        // 如果没有找到URL，使用标准路径
        if (fileUrl == null || fileUrl.isEmpty()) {
            String standardPath = "reconstruction/" + id + "/" + fileName;
            if (fileStorageService.isFileExists(standardPath)) {
                fileUrl = fileStorageService.getFileUrl(standardPath);
                log.info("Using standard path URL: {}", fileUrl);
            }
        }

        // 如果没有找到对应的URL
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("URL not found for file: {} in task: {}", fileName, id);
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "文件URL不存在");
        }

        log.info("Redirecting to file URL: {}", fileUrl);

        // 返回重定向响应
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setLocation(URI.create(fileUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302重定向
        } catch (Exception e) {
            log.error("Failed to create redirect URI for URL: {}", fileUrl, e);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "创建重定向URL失败: " + e.getMessage());
            return null; // 这行代码不会执行，因为ThrowUtils会抛出异常
        }
    }

    /**
     * 获取文件URL（用于图片预览）
     * <p>
     * 这个接口用于获取文件URL，主要用于图片预览。
     * 它会重定向到文件的实际URL，而不是返回文件内容。
     * </p>
     *
     * @param filePath 文件路径
     * @return 重定向到文件URL的响应
     */
    @GetMapping("/preview")
    @Operation(summary = "获取文件URL", description = "根据文件路径获取文件URL，主要用于图片预览")
    public ResponseEntity<Void> getFileContent(@RequestParam("path") String filePath) {
        // 检查路径是否存在
        ThrowUtils.throwIf(filePath == null || filePath.isEmpty(), ErrorCode.PARAMS_ERROR, "文件路径不能为空");

        log.info("Getting file URL for path: {}", filePath);

        try {
            // 如果是完整URL，直接重定向
            if (filePath.startsWith("http")) {
                log.info("Path is already a URL, redirecting directly: {}", filePath);
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(filePath));
                return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302重定向
            }

            // 如果是相对路径，获取完整URL
            String fileUrl = fileStorageService.getFileUrl(filePath);
            if (fileUrl == null || fileUrl.isEmpty()) {
                log.warn("Could not get URL for path: {}", filePath);
                ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "文件URL不存在");
            }

            log.info("Redirecting to file URL: {}", fileUrl);

            // 返回重定向响应
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(fileUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302重定向
        } catch (Exception e) {
            log.error("Failed to get URL for path: {}", filePath, e);
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "获取文件URL失败: " + e.getMessage());
            return null; // 这行代码不会执行，因为ThrowUtils会抛出异常
        }
    }

    /**
     * 获取回调URL
     *
     * @param request HTTP请求
     * @return 回调URL
     */
    @Value("${reconstruction.callback.host:}")
    private String callbackHost;

    @Value("${reconstruction.callback.default-ip:10.0.0.123}")
    private String defaultCallbackIp;

    /**
     * 获取回调URL
     * <p>
     * 构建Python服务可访问的回调URL。优先级如下：
     * 1. 使用配置的回调主机名（reconstruction.callback.host）
     * 2. 如果是localhost或127.0.0.1，使用配置的默认IP（reconstruction.callback.default-ip）
     * 3. 尝试自动检测本机IP
     * 4. 如果自动检测失败，使用配置的默认IP
     * </p>
     *
     * @param request HTTP请求
     * @return 回调URL
     */
    private String getCallbackUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName;

        // 1. 优先使用配置的回调主机名
        if (callbackHost != null && !callbackHost.isEmpty()) {
            serverName = callbackHost;
            log.info("使用配置的回调主机名: {}", serverName);
        } else {
            serverName = request.getServerName();

            // 如果是本地开发环境
            if ("localhost".equals(serverName) || "127.0.0.1".equals(serverName)) {
                // 2. 使用配置的默认IP
                if (defaultCallbackIp != null && !defaultCallbackIp.isEmpty()) {
                    serverName = defaultCallbackIp;
                    log.info("使用配置的默认IP地址: {}", serverName);
                } else {
                    // 3. 尝试自动检测本机IP
                    try {
                        serverName = InetAddress.getLocalHost().getHostAddress();
                        log.info("自动检测到本机IP地址: {}", serverName);
                    } catch (UnknownHostException e) {
                        // 4. 如果自动检测失败，使用硬编码的默认IP
                        serverName = "10.0.0.123";
                        log.warn("无法获取本机IP地址，使用硬编码的默认IP: {}", serverName);
                    }
                }
            }
        }

        // 构建回调URL
        String callbackUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + "/reconstruction/callback";
        log.info("生成回调URL: {}", callbackUrl);
        return callbackUrl;
    }

    /**
     * 异步处理重建任务
     * <p>
     * 使用Spring的@Async注解实现异步处理，在指定的线程池中执行
     * </p>
     *
     * @param taskId      任务ID（字符串形式）
     * @param imageData   图片数据
     * @param callbackUrl 回调URL
     */
    @Async("reconstructionTaskExecutor")
    public void processReconstructionTaskAsync(String taskId, byte[] imageData, String callbackUrl) {
        try {
            log.info("Starting async processing for task: {}", taskId);

            // 将taskId转换为Long类型
            Long taskIdLong = Long.parseLong(taskId);

            // 更新任务状态为处理中
            reconstructionTaskService.updateTaskStatus(taskIdLong, TaskStatus.PROCESSING, null);

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
                        reconstructionTaskService.updateTaskStatus(taskIdLong, TaskStatus.FAILED, ex.getMessage());
                        // 发送SSE状态更新
                        eventStreamService.sendStatusEvent(taskId, TaskStatus.FAILED, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Failed to process image for task: {}", taskId, e);
            try {
                // 将taskId转换为Long类型
                Long taskIdLong = Long.parseLong(taskId);
                // 更新任务状态为失败
                reconstructionTaskService.updateTaskStatus(taskIdLong, TaskStatus.FAILED, e.getMessage());
            } catch (NumberFormatException nfe) {
                log.error("Invalid task ID format: {}", taskId, nfe);
            }
            // 发送SSE状态更新
            eventStreamService.sendStatusEvent(taskId, TaskStatus.FAILED, e.getMessage());
        }
    }
}
