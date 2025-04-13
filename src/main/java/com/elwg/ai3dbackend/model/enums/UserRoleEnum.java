package com.elwg.ai3dbackend.model.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    /**
     * 普通用户
     */
    USER("user", "用户"),

    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),

    /**
     * 被封号
     */
    BAN("ban", "被封号");

    /**
     * 角色编码
     */
    private final String value;

    /**
     * 角色名称
     */
    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 角色编码
     * @return 对应的枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
