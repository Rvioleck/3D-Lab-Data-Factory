package com.elwg.ai3dbackend.model.dto;

import com.elwg.ai3dbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserQueryRequest", description = "用户查询请求")
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1234567890123456789")
    private Long id;

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
     * 用户角色：user/admin/ban
     */
    @Schema(description = "用户角色", example = "user", allowableValues = {"user", "admin", "ban"})
    private String userRole;

    /**
     * 创建时间范围（开始）
     */
    @Schema(description = "创建时间范围（开始）", example = "2023-01-01 00:00:00")
    private Date createTimeStart;

    /**
     * 创建时间范围（结束）
     */
    @Schema(description = "创建时间范围（结束）", example = "2025-12-31 23:59:59")
    private Date createTimeEnd;
}
