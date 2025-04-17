package com.elwg.ai3dbackend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 3D重建响应对象
 * <p>
 * 用于返回3D重建的结果信息
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ReconstructionResponse", description = "3D重建响应对象")
public class ReconstructionResponse implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID", example = "1234567890123456789")
    private String taskId;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态", example = "success", notes = "可能的值：success（成功）、failed（失败）")
    private String status;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息", example = "处理失败：图片格式不支持")
    private String errorMessage;

    /**
     * 像素图像URL
     */
    @ApiModelProperty(value = "像素图像URL", example = "/api/reconstruction/files/1234567890123456789/pixel_images.png")
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    @ApiModelProperty(value = "XYZ图像URL", example = "/api/reconstruction/files/1234567890123456789/xyz_images.png")
    private String xyzImagesUrl;

    /**
     * OBJ文件URL
     */
    @ApiModelProperty(value = "OBJ文件URL", example = "/api/reconstruction/files/1234567890123456789/model.obj")
    private String objFileUrl;

    /**
     * MTL文件URL
     */
    @ApiModelProperty(value = "MTL文件URL", example = "/api/reconstruction/files/1234567890123456789/model.mtl")
    private String mtlFileUrl;

    /**
     * 纹理图像URL
     */
    @ApiModelProperty(value = "纹理图像URL", example = "/api/reconstruction/files/1234567890123456789/texture.png")
    private String textureImageUrl;
}
