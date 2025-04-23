package com.elwg.ai3dbackend.model.dto.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 模型更新请求
 */
@Data
@ApiModel(value = "模型更新请求", description = "用于更新模型信息的请求参数")
public class ModelUpdateRequest {

    /**
     * 模型ID
     */
    @ApiModelProperty(value = "模型ID", required = true, example = "1")
    private Long id;

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称", example = "显微镜模型")
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介", example = "这是一个高精度的显微镜3D模型")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", example = "实验器材")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @ApiModelProperty(value = "标签", example = "显微镜,科学,实验")
    private String tags;
}
