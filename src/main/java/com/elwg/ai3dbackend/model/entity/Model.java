package com.elwg.ai3dbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 3D模型实体
 */
@TableName(value = "model")
@Data
public class Model implements Serializable {

    /**
     * 序列化版本号
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 源图片ID
     */
    private Long sourceImageId;

    /**
     * OBJ文件URL
     */
    private String objFileUrl;

    /**
     * MTL文件URL
     */
    private String mtlFileUrl;

    /**
     * 纹理图像URL
     */
    private String textureImageUrl;

    /**
     * 像素图像URL
     */
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    private String xyzImagesUrl;

    /**
     * 模型文件大小（字节）
     */
    private Long modelSize;

    /**
     * 模型格式
     */
    private String modelFormat;

    /**
     * 重建任务ID（雪花ID的字符串表示）
     */
    private String taskId;

    /**
     * 状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDelete;
}
