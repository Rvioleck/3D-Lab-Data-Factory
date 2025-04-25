package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3D重建上传请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "3D重建上传请求")
public class ReconstructionUploadRequest {

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "我的3D模型")
    private String name;

    /**
     * 模型简介
     */
    @Schema(description = "模型简介", example = "这是一个3D模型的简介")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "家具")
    private String category;

    /**
     * 标签，JSON数组字符串
     */
    @Schema(description = "标签，JSON数组字符串", example = "[\"家具\",\"椅子\"]")
    private String tags;
}
