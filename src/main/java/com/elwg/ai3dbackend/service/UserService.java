package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务接口
 * 定义用户模块对外暴露的核心业务功能
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 验证用户注册信息，创建新用户
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录（返回LoginUserVO）
     * 验证用户登录信息，记录登录状态，返回脱敏的用户视图对象
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求对象，用于存储会话信息
     * @return 登录用户视图对象
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * 从会话中获取当前登录用户信息
     *
     * @param request HTTP请求对象
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户脱敏
     * 移除用户敏感信息，返回可安全传输的用户对象
     *
     * @param originUser 原始用户对象
     * @return 脱敏后的用户对象
     */
    User getSafetyUser(User originUser);
}
