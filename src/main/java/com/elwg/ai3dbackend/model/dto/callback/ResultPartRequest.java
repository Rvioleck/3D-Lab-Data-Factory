package com.elwg.ai3dbackend.model.dto.callback;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "结果部分请求")
public class ResultPartRequest {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskId;

    /**
     * 结果部分名称
     * 例如：pixel_images.png, xyz_images.png, output3d.zip
     */
    @Schema(description = "结果部分名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "pixel_images.png")
    private String name;

    /**
     * 内容类型
     * 例如：image/png, application/zip
     */
    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "image/png")
    private String contentType;
}



