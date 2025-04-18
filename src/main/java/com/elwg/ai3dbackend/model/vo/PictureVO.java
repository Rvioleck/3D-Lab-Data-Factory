package com.elwg.ai3dbackend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片视图对象
 */
@Data
@ApiModel(description = "图片视图对象")
public class PictureVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    @ApiModelProperty(value = "图片ID", example = "1")
    private Long id;

    /**
     * 图片 url
     */
    @ApiModelProperty(value = "图片URL", example = "https://example.com/image.jpg")
    private String url;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称", example = "示例图片")
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介", example = "这是一张示例图片")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", example = "家具")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @ApiModelProperty(value = "标签", example = "桌子,椅子")
    private String tags;

    /**
     * 图片体积
     */
    @ApiModelProperty(value = "图片体积(字节)", example = "1024000")
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty(value = "图片宽度(像素)", example = "1920")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty(value = "图片高度(像素)", example = "1080")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @ApiModelProperty(value = "图片宽高比例", example = "1.78")
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty(value = "图片格式", example = "jpg")
    private String picFormat;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户ID", example = "1")
    private Long userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", example = "2023-01-01 12:00:00")
    private Date createTime;

    /**
     * 是否有关联的3D模型
     */
    @ApiModelProperty(value = "是否有关联的3D模型", example = "true")
    private Boolean hasModel;
}
