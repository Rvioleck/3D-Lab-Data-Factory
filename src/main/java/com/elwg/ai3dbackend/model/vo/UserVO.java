package com.elwg.ai3dbackend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象
 * <p>
 * 用于返回脱敏后的用户信息，不包含敏感字段如密码等
 * </p>
 */
@Data
@ApiModel(value = "UserVO", description = "用户视图对象")
public class UserVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", example = "1234567890123456789")
    private Long id;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", example = "zhangsan")
    private String userAccount;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", example = "张三")
    private String userName;

    /**
     * 用户头像URL
     */
    @ApiModelProperty(value = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介", example = "这是一个用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色", example = "user", notes = "可选值：user（普通用户）、admin（管理员）、ban（已封禁）")
    private String userRole;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2023-01-01 12:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", example = "2023-01-01 12:00:00")
    private Date updateTime;
}
