package com.elwg.ai3dbackend.model.dto.model;

import com.elwg.ai3dbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "模型查询请求", description = "用于分页查询模型的请求参数")
public class ModelQueryRequest extends PageRequest {

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "显微镜模型")
    private String name;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "实验器材")
    private String category;

    /**
     * 标签
     */
    @Schema(description = "标签", example = "显微镜,科学,实验")
    private String tags;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    @Schema(description = "状态", example = "COMPLETED", allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
    private String status;
}
