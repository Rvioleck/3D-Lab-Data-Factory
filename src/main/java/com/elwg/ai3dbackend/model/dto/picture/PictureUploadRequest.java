package com.elwg.ai3dbackend.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 图片上传请求DTO
 * <p>
 * 用于封装图片上传接口的请求参数，包括图片文件和相关元数据。
 * 支持以下功能：
 * 1. 上传图片文件（必需）
 * 2. 设置图片名称（可选）
 * 3. 设置图片分类（可选）
 * 4. 添加图片标签（可选）
 * 5. 添加图片简介（可选）
 * </p>
 *
 * @author [your-name]
 */
@Data
@Schema(description = "图片上传请求")
public class PictureUploadRequest {
    
    /**
     * 图片文件
     * 支持的格式：JPG、PNG、GIF等常见图片格式
     * 大小限制：10MB
     */
    @NotNull(message = "图片文件不能为空")
    @Schema(description = "图片文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;
    
    /**
     * 图片名称
     * 如果不提供，将使用原始文件名
     */
    @Size(max = 100, message = "图片名称不能超过100个字符")
    @Schema(description = "图片名称（可选）", example = "100ml锥形瓶001")
    private String name;
    
    /**
     * 图片分类
     * 用于对图片进行分类管理
     */
    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "图片分类（可选）", example = "锥形瓶")
    private String category;
    
    /**
     * 图片标签
     * 多个标签用逗号分隔，如：风景,自然,山水
     */
    @Size(max = 200, message = "标签不能超过200个字符")
    @Schema(description = "图片标签，多个标签用逗号分隔（可选）", example = "容器,锥形瓶")
    private String tags;
    
    /**
     * 图片简介
     * 用于详细描述图片内容或背景
     */
    @Size(max = 500, message = "简介不能超过500个字符")
    @Schema(description = "图片简介（可选）", example = "容积为100ml锥形瓶其中存放着20ml的红色透明液体")
    private String introduction;
}