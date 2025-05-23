package com.elwg.ai3dbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@TableName(value = "chat_session")
@Data
public class ChatSession implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    
    private String sessionName;
    
    private Date createTime;
    
    private Date updateTime;

    @TableLogic
    private Integer isDelete;
}