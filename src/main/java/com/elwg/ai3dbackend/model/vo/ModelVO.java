package com.elwg.ai3dbackend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 模型视图对象
 */
@Data
@ApiModel(value = "模型视图对象", description = "用于前端展示的模型信息")
public class ModelVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "模型ID", example = "1")
    private Long id;

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称", example = "显微镜模型")
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介", example = "这是一个高精度的显微镜3D模型")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", example = "实验器材")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @ApiModelProperty(value = "标签", example = "显微镜,科学,实验")
    private String tags;

    /**
     * 源图片ID
     */
    @ApiModelProperty(value = "源图片ID", example = "1")
    private Long sourceImageId;

    /**
     * OBJ文件URL
     */
    @ApiModelProperty(value = "OBJ文件URL", example = "https://example.com/models/microscope.obj")
    private String objFileUrl;

    /**
     * MTL文件URL
     */
    @ApiModelProperty(value = "MTL文件URL", example = "https://example.com/models/microscope.mtl")
    private String mtlFileUrl;

    /**
     * 纹理图像URL
     */
    @ApiModelProperty(value = "纹理图像URL", example = "https://example.com/models/microscope.png")
    private String textureImageUrl;

    /**
     * 像素图像URL
     */
    @ApiModelProperty(value = "像素图像URL", example = "https://example.com/models/microscope_pixel.png")
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    @ApiModelProperty(value = "XYZ图像URL", example = "https://example.com/models/microscope_xyz.png")
    private String xyzImagesUrl;

    /**
     * 模型文件大小（字节）
     */
    @ApiModelProperty(value = "模型文件大小", example = "1024000")
    private Long modelSize;

    /**
     * 模型格式
     */
    @ApiModelProperty(value = "模型格式", example = "OBJ")
    private String modelFormat;

    /**
     * 重建任务ID
     */
    @ApiModelProperty(value = "重建任务ID", example = "abc123def456")
    private String taskId;

    /**
     * 状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    @ApiModelProperty(value = "状态", example = "COMPLETED")
    private String status;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户ID", example = "1")
    private Long userId;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2023-04-16T14:20:00Z")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", example = "2023-04-16T14:20:00Z")
    private Date updateTime;
}
