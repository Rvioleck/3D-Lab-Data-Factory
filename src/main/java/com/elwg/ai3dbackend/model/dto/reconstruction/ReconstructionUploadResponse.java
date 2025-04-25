package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3D重建上传响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "3D重建上传响应")
public class ReconstructionUploadResponse {

    /**
     * 任务ID
     * 客户端可以使用此ID构建SSE URL: /api/reconstruction/events/{taskId}
     */
    @Schema(description = "任务ID", example = "1")
    private Long taskId;
}



