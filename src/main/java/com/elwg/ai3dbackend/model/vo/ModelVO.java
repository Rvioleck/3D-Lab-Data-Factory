package com.elwg.ai3dbackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 模型视图对象
 */
@Data
@Schema(name = "模型视图对象", description = "用于前端展示的模型信息")
public class ModelVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "模型ID", example = "1")
    private Long id;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "显微镜模型")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介", example = "这是一个高精度的显微镜3D模型")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "实验器材")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @Schema(description = "标签", example = "显微镜,科学,实验")
    private String tags;

    /**
     * 源图片ID
     */
    @Schema(description = "源图片ID", example = "1")
    private Long sourceImageId;

    /**
     * OBJ文件URL
     */
    @Schema(description = "OBJ文件URL", example = "https://example.com/models/microscope.obj")
    private String objFileUrl;

    /**
     * MTL文件URL
     */
    @Schema(description = "MTL文件URL", example = "https://example.com/models/microscope.mtl")
    private String mtlFileUrl;

    /**
     * 纹理图像URL
     */
    @Schema(description = "纹理图像URL", example = "https://example.com/models/microscope.png")
    private String textureImageUrl;

    /**
     * 像素图像URL
     */
    @Schema(description = "像素图像URL", example = "https://example.com/models/microscope_pixel.png")
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    @Schema(description = "XYZ图像URL", example = "https://example.com/models/microscope_xyz.png")
    private String xyzImagesUrl;

    /**
     * 模型文件大小（字节）
     */
    @Schema(description = "模型文件大小", example = "1024000")
    private Long modelSize;

    /**
     * 模型格式
     */
    @Schema(description = "模型格式", example = "OBJ")
    private String modelFormat;

    /**
     * 重建任务ID
     */
    @Schema(description = "重建任务ID", example = "abc123def456")
    private String taskId;

    /**
     * 状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    @Schema(description = "状态", example = "COMPLETED")
    private String status;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户ID", example = "1")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-04-16T14:20:00Z")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-04-16T14:20:00Z")
    private Date updateTime;
}
