package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 3D重建任务DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "3D重建任务DTO")
public class ReconstructionTaskDTO {

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID", example = "1234567890abcdef1234567890abcdef")
    private String taskId;

    /**
     * 任务状态
     */
    @ApiModelProperty(value = "任务状态", example = "PROCESSING", notes = "可能的值：PENDING, PROCESSING, COMPLETED, FAILED")
    private String status;

    /**
     * 源图片ID
     */
    @ApiModelProperty(value = "源图片ID", example = "1")
    private Long sourceImageId;

    /**
     * 原始图片URL
     */
    @ApiModelProperty(value = "原始图片URL", example = "https://example.com/images/1234567890abcdef1234567890abcdef/image.jpg")
    private String originalImageUrl;

    /**
     * 像素图像URL
     */
    @ApiModelProperty(value = "像素图像URL", example = "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/pixel_images.png")
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    @ApiModelProperty(value = "XYZ图像URL", example = "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/xyz_images.png")
    private String xyzImagesUrl;

    /**
     * 输出ZIP包URL
     */
    @ApiModelProperty(value = "输出ZIP包URL", example = "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/output3d.zip")
    private String outputZipUrl;

    /**
     * 结果模型ID
     */
    @ApiModelProperty(value = "结果模型ID", example = "1")
    private Long resultModelId;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息", example = "处理失败：图片格式不支持")
    private String errorMessage;

    /**
     * 处理时间（秒）
     */
    @ApiModelProperty(value = "处理时间（秒）", example = "120")
    private Integer processingTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2023-11-15 12:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", example = "2023-11-15 12:02:00")
    private Date updateTime;
}
