package com.elwg.ai3dbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elwg.ai3dbackend.common.DeleteRequest;
import com.elwg.ai3dbackend.model.dto.UserCreateRequest;
import com.elwg.ai3dbackend.model.dto.UserPasswordUpdateRequest;
import com.elwg.ai3dbackend.model.dto.UserProfileUpdateRequest;
import com.elwg.ai3dbackend.model.dto.UserQueryRequest;
import com.elwg.ai3dbackend.model.dto.UserUpdateRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.UserDetailVO;
import com.elwg.ai3dbackend.model.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 用户登录
     * 验证用户登录信息，记录登录状态，返回脱敏的用户视图对象
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求对象，用于存储会话信息
     * @return 脱敏后的用户视图对象
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * 从会话中获取当前登录用户信息
     *
     * @param request HTTP请求对象
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 将用户实体转换为视图对象
     * 移除用户敏感信息，返回可安全传输的用户视图对象
     *
     * @param user 原始用户对象
     * @return 脱敏后的用户视图对象
     */
    UserVO toUserVO(User user);

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
     * 获取用户详细信息
     * 根据ID获取用户详细信息，并进行脱敏处理
     *
     * @param id 用户ID
     * @return 用户详细视图对象
     */
    UserDetailVO getUserDetail(Long id);

    /**
     * 将用户实体转换为详细视图对象
     * 移除用户敏感信息，返回包含更多信息的用户详细视图对象
     *
     * @param user 原始用户对象
     * @return 用户详细视图对象
     */
    UserDetailVO toUserDetailVO(User user);

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

    /**
     * 物理删除用户
     * 完全从数据库中删除用户记录，而不是逻辑删除
     * 该方法可以删除已经逻辑删除的用户，也可以删除正常用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean physicalDeleteUser(Long id);

    /**
     * 更新用户个人资料
     * 允许用户更新自己的个人资料，包括账号、用户名、头像和简介
     *
     * @param profileUpdateRequest 个人资料更新请求
     * @param request HTTP请求对象
     * @return 更新后的用户视图对象
     */
    UserVO updateUserProfile(UserProfileUpdateRequest profileUpdateRequest, HttpServletRequest request);

    /**
     * 管理员更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @param request HTTP请求对象
     * @return 是否更新成功
     */
    boolean adminUpdateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 验证并创建新用户
     *
     * @param userCreateRequest 用户创建请求
     * @param request HTTP请求对象
     * @return 新用户ID
     */
    Long createUserByAdmin(UserCreateRequest userCreateRequest, HttpServletRequest request);

    /**
     * 更新用户密码
     *
     * @param passwordUpdateRequest 密码更新请求
     * @param request HTTP请求对象
     * @return 是否更新成功
     */
    boolean updateUserPassword(UserPasswordUpdateRequest passwordUpdateRequest, HttpServletRequest request);

    /**
     * 删除用户（管理员操作）
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求对象
     * @return 是否删除成功
     */
    boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 获取所有用户信息（管理员操作）
     *
     * @return 所有用户信息列表
     */
    List<UserVO> listAllUsers();
}
