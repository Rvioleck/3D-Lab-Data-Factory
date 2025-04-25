package com.elwg.ai3dbackend.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 图片更新请求
 */
@Data
@Schema(description = "图片更新请求")
public class PictureUpdateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    @Schema(description = "图片ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称", example = "示例图片")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "简介", example = "这是一张示例图片")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "家具")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @Schema(description = "标签", example = "桌子,椅子")
    private String tags;
}
