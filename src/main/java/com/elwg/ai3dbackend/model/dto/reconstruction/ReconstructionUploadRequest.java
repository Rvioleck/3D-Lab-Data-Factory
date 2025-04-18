package com.elwg.ai3dbackend.model.dto.reconstruction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3D重建上传请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "3D重建上传请求")
public class ReconstructionUploadRequest {

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称", example = "我的3D模型")
    private String name;

    /**
     * 模型简介
     */
    @ApiModelProperty(value = "模型简介", example = "这是一个3D模型的简介")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", example = "家具")
    private String category;

    /**
     * 标签，JSON数组字符串
     */
    @ApiModelProperty(value = "标签，JSON数组字符串", example = "[\"家具\",\"椅子\"]")
    private String tags;
}
