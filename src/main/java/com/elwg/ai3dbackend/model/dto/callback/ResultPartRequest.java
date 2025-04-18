package com.elwg.ai3dbackend.model.dto.callback;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结果部分请求
 * <p>
 * 用于接收Python服务发送的处理结果部分
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "结果部分请求")
public class ResultPartRequest {

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID", required = true)
    private String taskId;

    /**
     * 结果部分名称
     * 例如：pixel_images.png, xyz_images.png, output3d.zip
     */
    @ApiModelProperty(value = "结果部分名称", required = true, example = "pixel_images.png")
    private String name;

    /**
     * 内容类型
     * 例如：image/png, application/zip
     */
    @ApiModelProperty(value = "内容类型", required = true, example = "image/png")
    private String contentType;
}
