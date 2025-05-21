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
     * 主键，雪花ID，同时作为任务唯一标识符
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 源图片ID
     */
    private Long sourceImageId;

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
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
