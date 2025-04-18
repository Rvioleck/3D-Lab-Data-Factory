package com.elwg.ai3dbackend.model.dto.callback;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "状态更新请求")
public class StatusUpdateRequest {

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID", required = true)
    private String taskId;

    /**
     * 状态
     * 可能的值：processing, completed, failed
     */
    @ApiModelProperty(value = "状态", required = true, allowableValues = "processing, completed, failed")
    private String status;

    /**
     * 错误信息（当状态为failed时）
     */
    @ApiModelProperty(value = "错误信息（当状态为failed时）")
    private String error;
}
