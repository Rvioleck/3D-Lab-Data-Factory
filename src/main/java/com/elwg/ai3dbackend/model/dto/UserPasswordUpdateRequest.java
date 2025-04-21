package com.elwg.ai3dbackend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码更新请求
 * <p>
 * 用于用户更新自己的密码
 * </p>
 */
@Data
@ApiModel(value = "UserPasswordUpdateRequest", description = "用户密码更新请求")
public class UserPasswordUpdateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    @ApiModelProperty(value = "旧密码", required = true, example = "oldpassword123")
    private String oldPassword;

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码", required = true, example = "newpassword123")
    private String newPassword;

    /**
     * 确认新密码
     */
    @ApiModelProperty(value = "确认新密码", required = true, example = "newpassword123")
    private String checkPassword;
}
