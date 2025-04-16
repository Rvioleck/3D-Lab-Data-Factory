package com.elwg.ai3dbackend.aop;

import cn.hutool.core.util.StrUtil;
import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 * 用于拦截带有 AuthCheck 注解的类或方法，进行权限校验
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 环绕通知，拦截带有 AuthCheck 注解的方法
     *
     * @param joinPoint 切点
     * @param authCheck 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        return checkAuth(joinPoint, authCheck.mustRole());
    }

    /**
     * 环绕通知，拦截带有 AuthCheck 注解的类中的所有方法
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@within(com.elwg.ai3dbackend.annotation.AuthCheck)")
    public Object doClassInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取类上的注解
        Class<?> clazz = joinPoint.getTarget().getClass();
        AuthCheck authCheck = clazz.getAnnotation(AuthCheck.class);
        if (authCheck != null) {
            return checkAuth(joinPoint, authCheck.mustRole());
        }
        return joinPoint.proceed();
    }

    /**
     * 权限校验方法
     *
     * @param joinPoint 切点
     * @param mustRole 必须具有的角色
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    private Object checkAuth(ProceedingJoinPoint joinPoint, String mustRole) throws Throwable {
        // 1. 已获取必须的角色

        // 2. 获取 request
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 3. 获取当前登录用户（会自动校验是否登录）
        User loginUser = userService.getLoginUser(request);

        // 4. 判断用户角色是否匹配
        String userRole = loginUser.getUserRole();

        // 5. 如果需要管理员权限但用户不是管理员，抛出无权限异常
        if (StrUtil.isNotBlank(mustRole)) {
            UserRoleEnum requiredRole = UserRoleEnum.getEnumByValue(mustRole);
            if (requiredRole == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "角色配置错误");
            }

            // 如果是管理员，允许操作
            if (UserRoleEnum.ADMIN.getValue().equals(userRole)) {
                return joinPoint.proceed();
            }

            // 必须有对应的角色才能操作
            if (!requiredRole.getValue().equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
