package com.elwg.ai3dbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 * <p>
 * 用于管理员创建新用户的请求参数
 * </p>
 */
@Data
@Schema(name = "UserCreateRequest", description = "用户创建请求")
public class UserCreateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    private String userAccount;

    /**
     * 用户密码
     */
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678")
    private String userPassword;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678")
    private String checkPassword;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "张三")
    private String userName;

    /**
     * 用户头像URL
     */
    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介", example = "这是一个用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @Schema(description = "用户角色", example = "user", allowableValues = {"user", "admin", "ban"}, implementation = String.class)
    private String userRole;
}
