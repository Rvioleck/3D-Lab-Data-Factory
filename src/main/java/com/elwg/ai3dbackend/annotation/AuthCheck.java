package com.elwg.ai3dbackend.annotation;

import com.elwg.ai3dbackend.constant.UserConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 使用说明：
 * 1. 使用此注解的类或方法会进行登录状态和角色权限的校验
 * 2. 不使用此注解的类或方法对所有用户开放，无需登录即可访问
 * 3. 可以加在类上或方法上，方法上的权限配置会覆盖类上的配置
 * 示例：
 * - @AuthCheck                    // 要求用户登录，且为普通用户及以上权限
 * - @AuthCheck(mustRole="admin")  // 要求用户登录，且必须是管理员
 *
 * @author fyz
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色才能访问
     * 默认为 "user"，表示普通用户及以上权限即可访问
     * 可选值：user/admin/ban 等，具体见 UserConstant
     */
    String mustRole() default UserConstant.USER_ROLE;
}
