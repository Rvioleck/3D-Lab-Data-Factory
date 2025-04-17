package com.elwg.ai3dbackend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户详细视图对象
 * <p>
 * 用于返回更详细的用户信息，在UserVO基础上增加额外的非敏感信息
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "UserDetailVO", description = "用户详细视图对象")
public class UserDetailVO extends UserVO {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 编辑时间
     */
    @ApiModelProperty(value = "编辑时间", example = "2023-01-01 12:00:00")
    private Date editTime;

    /**
     * 最后登录时间（可以在未来扩展）
     */
    @ApiModelProperty(value = "最后登录时间", example = "2023-01-01 12:00:00")
    private Date lastLoginTime;

    /**
     * 用户状态（可以在未来扩展）
     */
    @ApiModelProperty(value = "用户状态", example = "active", notes = "可选值：active（活跃）、inactive（非活跃）")
    private String userStatus;
}
