package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "3D重建任务DTO")
public class ReconstructionTaskDTO {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long id;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "PROCESSING", allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
    private String status;

    /**
     * 源图片ID
     */
    @Schema(description = "源图片ID", example = "1")
    private Long sourceImageId;

    /**
     * 原始图片URL
     */
    @Schema(description = "原始图片URL", example = "https://example.com/images/1/image.jpg")
    private String originalImageUrl;

    /**
     * 像素图像URL
     */
    @Schema(description = "像素图像URL", example = "https://example.com/reconstruction/1/pixel_images.png")
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    @Schema(description = "XYZ图像URL", example = "https://example.com/reconstruction/1/xyz_images.png")
    private String xyzImagesUrl;

    /**
     * 输出ZIP包URL
     */
    @Schema(description = "输出ZIP包URL", example = "https://example.com/reconstruction/1/output3d.zip")
    private String outputZipUrl;

    /**
     * 结果模型ID
     */
    @Schema(description = "结果模型ID", example = "1")
    private Long resultModelId;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "处理失败：图片格式不支持")
    private String errorMessage;

    /**
     * 处理时间（秒）
     */
    @Schema(description = "处理时间（秒）", example = "120")
    private Integer processingTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-11-15 12:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-11-15 12:02:00")
    private Date updateTime;
}
