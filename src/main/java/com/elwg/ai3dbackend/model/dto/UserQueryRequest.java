package com.elwg.ai3dbackend.model.dto;

import com.elwg.ai3dbackend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 * <p>
 * 用于查询用户列表的请求参数，支持分页和条件过滤
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "UserQueryRequest", description = "用户查询请求")
public class UserQueryRequest extends PageRequest implements Serializable {

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
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色", example = "user", notes = "可选值：user（普通用户）、admin（管理员）、ban（已封禁）")
    private String userRole;

    /**
     * 创建时间范围（开始）
     */
    @ApiModelProperty(value = "创建时间范围（开始）", example = "2023-01-01 00:00:00")
    private Date createTimeStart;

    /**
     * 创建时间范围（结束）
     */
    @ApiModelProperty(value = "创建时间范围（结束）", example = "2023-12-31 23:59:59")
    private Date createTimeEnd;
}
