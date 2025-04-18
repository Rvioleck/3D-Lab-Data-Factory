package com.elwg.ai3dbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 3D重建任务实体
 */
@TableName(value = "reconstruction_task")
@Data
public class ReconstructionTask implements Serializable {

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
     * 任务唯一标识符
     */
    private String taskId;

    /**
     * 任务状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 源图片ID
     */
    private Long sourceImageId;

    /**
     * 原始图片URL
     */
    private String originalImageUrl;

    /**
     * 像素图像URL
     */
    private String pixelImagesUrl;

    /**
     * XYZ图像URL
     */
    private String xyzImagesUrl;

    /**
     * 输出ZIP包URL
     */
    private String outputZipUrl;

    /**
     * 结果模型ID
     */
    private Long resultModelId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 处理时间（秒）
     */
    private Integer processingTime;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

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
