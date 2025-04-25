package com.elwg.ai3dbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码更新请求
 * <p>
 * 用于用户更新自己的密码
 * </p>
 */
@Data
@Schema(name = "UserPasswordUpdateRequest", description = "用户密码更新请求")
public class UserPasswordUpdateRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "oldpassword123")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "newpassword123")
    private String newPassword;

    /**
     * 确认新密码
     */
    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "newpassword123")
    private String checkPassword;
}
