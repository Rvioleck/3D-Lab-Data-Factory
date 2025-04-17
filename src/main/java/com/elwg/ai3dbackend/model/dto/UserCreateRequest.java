package com.elwg.ai3dbackend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 * <p>
 * 用于管理员创建新用户的请求参数
 * </p>
 */
@Data
@ApiModel(value = "UserCreateRequest", description = "用户创建请求")
public class UserCreateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true, example = "zhangsan")
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码", required = true, example = "12345678")
    private String userPassword;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码", required = true, example = "12345678")
    private String checkPassword;

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
}
