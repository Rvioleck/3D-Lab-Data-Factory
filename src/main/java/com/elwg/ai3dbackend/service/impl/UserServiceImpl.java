package com.elwg.ai3dbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.mapper.UserMapper;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.model.vo.LoginUserVO;
import com.elwg.ai3dbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务实现类
 * 实现用户模块的核心业务功能，包括用户注册、登录、信息获取等
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     * 验证用户注册信息，创建新用户账号
     * 包括参数校验、账号查重、密码加密和用户创建
     *
     * @param userAccount   用户账号，长度至少为4
     * @param userPassword  用户密码，长度至少为8
     * @param checkPassword 确认密码，必须与用户密码一致
     * @return 新用户的ID
     * @throws BusinessException 参数错误或账号重复时抛出异常
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账号");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2. 校验账号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 3. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        return user.getId();
    }

    /**
     * 用户登录验证
     * 验证用户账号和密码，返回脱敏后的用户信息
     * 该方法仅在内部使用，不对外暴露
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    private User userLoginVerify(String userAccount, String userPassword) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码错误");
        // 2. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = baseMapper.selectOne(queryWrapper);
        // 4. 用户不存在或被封号
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        ThrowUtils.throwIf(UserRoleEnum.BAN.getValue().equals(user.getUserRole()),
                ErrorCode.FORBIDDEN_ERROR, "该用户已被封号");
        // 5. 返回脱敏后的用户信息
        return getSafetyUser(user);
    }

    /**
     * 用户登录
     * 验证用户登录信息，记录登录状态，返回脱敏的用户视图对象
     * 该方法会在会话中记录用户的登录状态
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求对象，用于存储会话信息
     * @return 登录用户视图对象
     * @throws BusinessException 登录失败时抛出异常
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 调用登录验证方法获取脱敏后的用户信息
        User safetyUser = userLoginVerify(userAccount, userPassword);
        ThrowUtils.throwIf(safetyUser == null, ErrorCode.SYSTEM_ERROR, "登陆失败");
        // 2. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        // 3. 将脱敏后的用户信息转换为LoginUserVO对象
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(safetyUser, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     * 从会话中获取当前登录用户信息，并从数据库中查询最新的用户信息
     *
     * @param request HTTP请求对象，用于获取会话信息
     * @return 当前登录用户信息
     * @throws BusinessException 用户未登录时抛出异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 从会话中获取用户登录状态
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null || user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2. 防止单词会话中用户信息已经修改了，再查询一次数据库
        user = this.getById(user.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    /**
     * 用户脱敏
     * 移除敏感信息，如密码等，返回可安全传输的用户对象
     * 保留用户ID、账号、名称、头像、角色、简介、创建时间和更新时间
     *
     * @param originUser 原始用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAvatar(originUser.getUserAvatar());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserProfile(originUser.getUserProfile());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        return safetyUser;
    }

    /**
     * 获取加密密码
     * 使用MD5算法对密码进行加盐加密
     *
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    private String getEncryptPassword(String userPassword) {
        final String SALT = "elwg";
        return DigestUtils.md5DigestAsHex((SALT + userPassword + SALT).getBytes());
    }
}