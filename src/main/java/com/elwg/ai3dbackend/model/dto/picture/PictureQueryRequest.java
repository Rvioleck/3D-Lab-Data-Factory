package com.elwg.ai3dbackend.model.dto.picture;

import com.elwg.ai3dbackend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图片查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "图片查询请求")
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称", example = "示例图片")
    private String name;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", example = "家具")
    private String category;

    /**
     * 标签
     */
    @ApiModelProperty(value = "标签", example = "桌子,椅子")
    private String tags;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户ID", example = "1")
    private Long userId;
}
