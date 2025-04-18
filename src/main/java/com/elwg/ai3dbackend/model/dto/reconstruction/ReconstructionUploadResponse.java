package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3D重建上传响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "3D重建上传响应")
public class ReconstructionUploadResponse {

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID", example = "1234567890abcdef1234567890abcdef")
    private String taskId;

    /**
     * SSE URL
     */
    @ApiModelProperty(value = "SSE URL", example = "/api/reconstruction/events/1234567890abcdef1234567890abcdef")
    private String sseUrl;
}
