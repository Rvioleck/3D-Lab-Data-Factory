package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.model.dto.ReconstructionResponse;
import com.elwg.ai3dbackend.service.ModelFileService;
import com.elwg.ai3dbackend.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 3D模型文件服务实现类
 * <p>
 * 实现3D模型文件的处理和存储功能
 * </p>
 */
@Slf4j
@Service
public class ModelFileServiceImpl implements ModelFileService {

    private final StorageServiceFactory storageServiceFactory;

    // 存储每个任务的文件名映射
    private final Map<String, Map<String, String>> taskFileNames = new ConcurrentHashMap<>();

    // 存储每个任务的状态
    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();

    // 存储每个任务已接收的结果部分
    private final Map<String, Set<String>> taskReceivedParts = new ConcurrentHashMap<>();

    // 必要的结果部分列表，用于判断任务是否完成
    private final Set<String> requiredResultParts = new HashSet<String>() {{
        add("pixel_images.png");
        add("xyz_images.png");
        add("output3d.zip");
    }};

    @Autowired
    public ModelFileServiceImpl(StorageServiceFactory storageServiceFactory) {
        this.storageServiceFactory = storageServiceFactory;
    }

    /**
     * 处理3D重建结果部分
     * <p>
     * 处理渐进式接收的结果部分，并存储到指定的存储服务
     * </p>
     *
     * @param taskId 任务ID
     * @param name 结果部分名称（例如：pixel_images.png）
     * @param contentType 内容类型
     * @param data 二进制数据
     * @return 包含处理结果的响应对象
     * @throws IOException 如果文件处理过程中发生错误
     */
    @Override
    public ReconstructionResponse processResultPart(String taskId, String name, String contentType, byte[] data) throws IOException {
        log.info("Processing result part: {} for task: {}, content type: {}, size: {} bytes", name, taskId, contentType, data.length);
        StorageService storageService = storageServiceFactory.getStorageService();

        // 记录已接收的部分
        Set<String> receivedParts = taskReceivedParts.computeIfAbsent(taskId, k -> new HashSet<>());
        receivedParts.add(name);

        // 处理不同类型的结果部分
        if ("output3d.zip".equals(name)) {
            // 处理output3d.zip
            processOutput3dZip(taskId, data);
        } else {
            // 其他文件直接保存
            storageService.saveFile(getStoragePath(taskId, name), data);
        }

        // 检查是否所有必要的部分都已接收
        boolean isCompleted = isTaskCompleted(taskId);
        if (isCompleted) {
            taskStatus.put(taskId, "completed");
        } else {
            taskStatus.put(taskId, "processing");
        }

        // 构建响应对象
        ReconstructionResponse response = new ReconstructionResponse();
        response.setTaskId(taskId);
        response.setStatus(taskStatus.getOrDefault(taskId, "processing"));

        // 设置已接收文件的URL
        if (receivedParts.contains("pixel_images.png")) {
            response.setPixelImagesUrl(getFileUrl(taskId, "pixel_images.png"));
        }
        if (receivedParts.contains("xyz_images.png")) {
            response.setXyzImagesUrl(getFileUrl(taskId, "xyz_images.png"));
        }

        // 如果已完成，设置3D模型文件的URL
        if (isCompleted) {
            Map<String, String> fileMapping = taskFileNames.get(taskId);
            if (fileMapping != null) {
                if (fileMapping.containsKey("obj")) {
                    response.setObjFileUrl(getFileUrl(taskId, fileMapping.get("obj")));
                }
                if (fileMapping.containsKey("mtl")) {
                    response.setMtlFileUrl(getFileUrl(taskId, fileMapping.get("mtl")));
                }
                if (fileMapping.containsKey("texture")) {
                    response.setTextureImageUrl(getFileUrl(taskId, fileMapping.get("texture")));
                }
            } else {
                // 如果映射不存在，使用默认文件名
                response.setObjFileUrl(getFileUrl(taskId, "model.obj"));
                response.setMtlFileUrl(getFileUrl(taskId, "model.mtl"));
                response.setTextureImageUrl(getFileUrl(taskId, "texture.png"));
            }
        }

        return response;
    }

    /**
     * 处理3D重建结果ZIP文件（旧方法，保留向后兼容性）
     * <p>
     * 解析ZIP文件，提取其中的各个文件，并存储到指定的存储服务
     * </p>
     *
     * @param taskId  任务ID
     * @param zipData ZIP文件的二进制数据
     * @return 包含各个文件URL的响应对象
     * @throws IOException 如果文件处理过程中发生错误
     * @deprecated 使用 {@link #processResultPart(String, String, String, byte[])} 代替
     */
    @Override
    @Deprecated
    public ReconstructionResponse processReconstructionResult(String taskId, byte[] zipData) throws IOException {
        StorageService storageService = storageServiceFactory.getStorageService();

        // 提取主ZIP文件中的内容
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            byte[] pixelImagesData = null;
            byte[] xyzImagesData = null;
            byte[] output3dZipData = null;

            // 提取主ZIP中的文件
            while ((entry = zipIn.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (fileName.equals("pixel_images.png")) {
                    pixelImagesData = readAllBytes(zipIn);
                    storageService.saveFile(getStoragePath(taskId, "pixel_images.png"), pixelImagesData);
                } else if (fileName.equals("xyz_images.png")) {
                    xyzImagesData = readAllBytes(zipIn);
                    storageService.saveFile(getStoragePath(taskId, "xyz_images.png"), xyzImagesData);
                } else if (fileName.equals("output3d.zip")) {
                    output3dZipData = readAllBytes(zipIn);
                    // 不保存output3d.zip，而是解压它
                }
                zipIn.closeEntry();
            }

            // 解析内部ZIP文件
            if (output3dZipData != null) {
                processOutput3dZip(taskId, output3dZipData);
            }

            // 构建响应对象
            ReconstructionResponse response = new ReconstructionResponse();
            response.setTaskId(taskId);
            response.setStatus("success");

            // 设置图片文件的URL
            response.setPixelImagesUrl(getFileUrl(taskId, "pixel_images.png"));
            response.setXyzImagesUrl(getFileUrl(taskId, "xyz_images.png"));

            // 从内存中获取文件名映射
            Map<String, String> fileMapping = taskFileNames.get(taskId);
            if (fileMapping != null) {
                // 设置3D模型文件的URL
                if (fileMapping.containsKey("obj")) {
                    response.setObjFileUrl(getFileUrl(taskId, fileMapping.get("obj")));
                }
                if (fileMapping.containsKey("mtl")) {
                    response.setMtlFileUrl(getFileUrl(taskId, fileMapping.get("mtl")));
                }
                if (fileMapping.containsKey("texture")) {
                    response.setTextureImageUrl(getFileUrl(taskId, fileMapping.get("texture")));
                }
            } else {
                // 如果映射不存在，使用默认文件名
                response.setObjFileUrl(getFileUrl(taskId, "model.obj"));
                response.setMtlFileUrl(getFileUrl(taskId, "model.mtl"));
                response.setTextureImageUrl(getFileUrl(taskId, "texture.png"));
            }

            return response;
        }
    }

    /**
     * 处理output3d.zip文件
     *
     * @param taskId         任务ID
     * @param output3dZipData output3d.zip的二进制数据
     * @throws IOException 如果文件处理过程中发生错误
     */
    private void processOutput3dZip(String taskId, byte[] output3dZipData) throws IOException {
        StorageService storageService = storageServiceFactory.getStorageService();

        // 初始化文件名映射
        Map<String, String> fileMapping = new HashMap<>();

        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(output3dZipData))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String fileName = entry.getName();
                byte[] fileData = readAllBytes(zipIn);

                // 保存文件并记录文件名
                storageService.saveFile(getStoragePath(taskId, fileName), fileData);

                // 根据文件类型记录文件名
                if (fileName.endsWith(".obj")) {
                    fileMapping.put("obj", fileName);
                } else if (fileName.endsWith(".mtl")) {
                    fileMapping.put("mtl", fileName);
                } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    fileMapping.put("texture", fileName);
                }

                zipIn.closeEntry();
            }
        }

        // 将文件名映射存储在内存中
        if (!fileMapping.isEmpty()) {
            taskFileNames.put(taskId, fileMapping);
        }
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
     * 读取输入流中的所有字节
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException 如果读取过程中发生错误
     */
    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * 获取文件的物理路径
     *
     * @param taskId   任务ID
     * @param fileName 文件名
     * @return 文件的物理路径
     */
    @Override
    public String getFilePath(String taskId, String fileName) {
        StorageService storageService = storageServiceFactory.getStorageService();
        return storageService.getPhysicalPath(getStoragePath(taskId, fileName));
    }

    /**
     * 获取文件的URL路径
     *
     * @param taskId   任务ID
     * @param fileName 文件名
     * @return 文件的URL路径
     */
    @Override
    public String getFileUrl(String taskId, String fileName) {
        StorageService storageService = storageServiceFactory.getStorageService();
        return storageService.getFileUrl(getStoragePath(taskId, fileName));
    }

    /**
     * 检查任务结果是否存在
     *
     * @param taskId 任务ID
     * @return 如果任务结果存在，则返回true
     */
    @Override
    public boolean isTaskResultExists(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return false;
        }

        // 检查是否有任何已接收的部分
        Set<String> receivedParts = taskReceivedParts.get(taskId);
        return receivedParts != null && !receivedParts.isEmpty();
    }

    /**
     * 检查任务是否完成
     * <p>
     * 完成意味着所有必要的结果部分都已经接收并处理
     * </p>
     *
     * @param taskId 任务ID
     * @return 如果任务已完成，则返回true
     */
    @Override
    public boolean isTaskCompleted(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return false;
        }

        // 检查是否所有必要的部分都已接收
        Set<String> receivedParts = taskReceivedParts.get(taskId);
        if (receivedParts == null) {
            return false;
        }

        return receivedParts.containsAll(requiredResultParts);
    }

    /**
     * 获取任务的当前状态
     *
     * @param taskId 任务ID
     * @return 任务状态（processing, completed, failed）
     */
    @Override
    public String getTaskStatus(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return "unknown";
        }

        return taskStatus.getOrDefault(taskId, "processing");
    }

    /**
     * 获取任务的所有文件
     *
     * @param taskId 任务ID
     * @return 文件名到文件路径的映射
     */
    @Override
    public Map<String, String> getTaskFiles(String taskId) {
        Map<String, String> files = new HashMap<>();
        if (!StringUtils.hasText(taskId)) {
            return files;
        }

        StorageService storageService = storageServiceFactory.getStorageService();
        return storageService.listFiles("reconstruction/" + taskId);
    }

    /**
     * 获取文件数据
     *
     * @param taskId   任务ID
     * @param fileName 文件名
     * @return 文件数据的字节数组
     * @throws IOException 如果读取过程中发生错误
     */
    @Override
    public byte[] getFileData(String taskId, String fileName) throws IOException {
        StorageService storageService = storageServiceFactory.getStorageService();
        return storageService.getFileData(getStoragePath(taskId, fileName));
    }
}
