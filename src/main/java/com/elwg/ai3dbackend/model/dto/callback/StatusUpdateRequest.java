package com.elwg.ai3dbackend.model.dto.callback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态更新请求
 * <p>
 * 用于接收Python服务发送的任务状态更新
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态更新请求")
public class StatusUpdateRequest {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    /**
     * 状态
     * 可能的值：processing, completed, failed
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"processing", "completed", "failed"})
    private String status;

    /**
     * 错误信息（当状态为failed时）
     */
    @Schema(description = "错误信息（当状态为failed时）")
    private String error;
}
