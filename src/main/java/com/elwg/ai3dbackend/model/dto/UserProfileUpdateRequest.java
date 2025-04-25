package com.elwg.ai3dbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人资料更新请求
 * <p>
 * 用于用户更新自己的个人资料
 * </p>
 */
@Data
@Schema(name = "UserProfileUpdateRequest", description = "用户个人资料更新请求")
public class UserProfileUpdateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "zhangsan")
    private String userAccount;

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
}
