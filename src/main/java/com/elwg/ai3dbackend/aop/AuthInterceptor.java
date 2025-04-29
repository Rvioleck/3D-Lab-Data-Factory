package com.elwg.ai3dbackend.aop;

import cn.hutool.core.util.StrUtil;
import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.model.vo.UserVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;

/**
 * 权限校验 AOP
 * 用于拦截带有 AuthCheck 注解的类或方法，进行权限校验
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 环绕通知，拦截带有 AuthCheck 注解的方法
     *
     * @param joinPoint 切点
     * @param methodAuth 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(methodAuth)")
    public Object doMethodInterceptor(ProceedingJoinPoint joinPoint, AuthCheck methodAuth) throws Throwable {
        return checkAuth(joinPoint, methodAuth.mustRole());
    }

    /**
     * 环绕通知，拦截带有 AuthCheck 注解的类中的所有方法
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@within(classAuth)")
    public Object doClassInterceptor(ProceedingJoinPoint joinPoint, AuthCheck classAuth) throws Throwable {
        // 获取方法上的注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AuthCheck methodAuth = method.getAnnotation(AuthCheck.class);
        // 如果方法上有注解，优先使用方法上的 mustRole
        String mustRole = methodAuth != null ? methodAuth.mustRole() : classAuth.mustRole();
        return checkAuth(joinPoint, mustRole);
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
        // 3. 从会话中获取当前登录用户
        HttpSession session = request.getSession();
        UserVO loginUser = (UserVO) session.getAttribute(UserConstant.USER_LOGIN_STATE);
        // 如果用户未登录，抛出异常
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

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
