package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elwg.ai3dbackend.model.dto.UserQueryRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.LoginUserVO;
import com.elwg.ai3dbackend.model.vo.UserVO;

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
     * 移除用户敏感信息，返回可安全传输的用户视图对象
     *
     * @param originUser 原始用户对象
     * @return 脱敏后的用户视图对象
     */
    LoginUserVO getSafetyUser(User originUser);

    /**
     * 用户退出
     * 清除用户登录状态
     *
     * @param request HTTP请求对象，用于清除会话信息
     * @return 退出结果，true 表示成功，false 表示失败
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * 根据ID获取用户信息，并进行脱敏处理
     *
     * @param id 用户ID
     * @return 脱敏后的用户视图对象
     */
    UserVO getUser(Long id);

    /**
     * 获取脱敏后的用户信息
     * 将 User 对象转换为 UserVO 对象，移除敏感信息
     *
     * @param user 原始用户对象
     * @return 脱敏后的用户视图对象
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户列表（分页）
     * 根据查询条件获取用户列表，并进行脱敏处理
     *
     * @param userQueryRequest 用户查询请求
     * @return 分页用户列表（脱敏）
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    /**
     * 根据条件构建用户查询条件
     * 将 UserQueryRequest 转换为 QueryWrapper
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件包装器
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 对密码进行加密
     * 使用系统的加密算法对密码进行加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    String encryptPassword(String password);
}
