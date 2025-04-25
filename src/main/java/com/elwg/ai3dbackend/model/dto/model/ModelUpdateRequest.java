package com.elwg.ai3dbackend.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型更新请求
 */
@Data
@Schema(name = "模型更新请求", description = "用于更新模型信息的请求参数")
public class ModelUpdateRequest {

    /**
     * 模型ID
     */
    @Schema(description = "模型ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "显微镜模型")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介", example = "这是一个高精度的显微镜3D模型")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "实验器材")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @Schema(description = "标签", example = "显微镜,科学,实验")
    private String tags;
}
