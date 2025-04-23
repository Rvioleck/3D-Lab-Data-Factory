package com.elwg.ai3dbackend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人资料更新请求
 * <p>
 * 用于用户更新自己的个人资料
 * </p>
 */
@Data
@ApiModel(value = "UserProfileUpdateRequest", description = "用户个人资料更新请求")
public class UserProfileUpdateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

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
}
