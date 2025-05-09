package com.elwg.ai3dbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.common.DeleteRequest;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.mapper.UserMapper;
import com.elwg.ai3dbackend.model.dto.UserCreateRequest;
import com.elwg.ai3dbackend.model.dto.UserPasswordUpdateRequest;
import com.elwg.ai3dbackend.model.dto.UserProfileUpdateRequest;
import com.elwg.ai3dbackend.model.dto.UserQueryRequest;
import com.elwg.ai3dbackend.model.dto.UserUpdateRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.model.vo.UserDetailVO;
import com.elwg.ai3dbackend.model.vo.UserVO;
import com.elwg.ai3dbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param userPassword  用户密码，长度至少为6
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
        ThrowUtils.throwIf(userPassword.length() < 6 || checkPassword.length() < 6,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2. 校验账号是否已存在（只检查未被逻辑删除的账号）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("isDelete", 0); // 只检查未被逻辑删除的账号
        long count = baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");

        // 3. 检查是否存在已被逻辑删除的同名账号，如果存在则物理删除它
        // 使用自定义的Mapper方法查询已被逻辑删除的用户
        User deletedUser = baseMapper.selectOneByAccountLogicDeleted(userAccount);

        if (deletedUser != null) {
            log.info("发现已被逻辑删除的同名账号: {}, 尝试物理删除", userAccount);

            // 查询所有已被逻辑删除的同名账号，而不仅仅是第一个
            List<User> deletedUsers = baseMapper.selectListByAccountLogicDeleted(userAccount);

            log.info("发现 {} 个已被逻辑删除的同名账号: {}", deletedUsers.size(), userAccount);

            // 逐个删除所有已被逻辑删除的同名账号
            for (User user : deletedUsers) {
                // 使用现有的物理删除方法，避免代码重复
                boolean deleteResult = physicalDeleteUser(user.getId());

                // 记录删除结果，但不中断流程
                if (!deleteResult) {
                    log.warn("物理删除已被逻辑删除的账号失败: ID={}, 账号={}",
                            user.getId(), userAccount);
                } else {
                    log.info("成功物理删除已被逻辑删除的账号: ID={}, 账号={}",
                            user.getId(), userAccount);
                }
            }

            // 再次检查是否还有同名账号（以防删除失败或有多个同名账号）
            long remainingCount = baseMapper.countByAccountIgnoreLogicDelete(userAccount);

            if (remainingCount > 0) {
                log.warn("删除后仍存在同名账号: {}, 数量: {}", userAccount, remainingCount);

                // 再次检查是否有未被逻辑删除的同名账号
                long activeCount = baseMapper.countByAccountActive(userAccount);

                // 如果还有未被逻辑删除的同名账号，则拒绝注册
                if (activeCount > 0) {
                    log.error("存在未被逻辑删除的同名账号: {}, 数量: {}, 拒绝注册", userAccount, activeCount);
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
                }
            }
        }
        // 4. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 5. 插入用户数据
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
     * 验证用户账号和密码，返回用户实体
     * 该方法仅在内部使用，不对外暴露
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 用户实体
     */
    private User userLoginVerify(String userAccount, String userPassword) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(userPassword.length() < 6,
                ErrorCode.PARAMS_ERROR, "密码错误");
        // 2. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在（只查询未被逻辑删除的用户）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("isDelete", 0); // 只查询未被逻辑删除的用户
        User user = baseMapper.selectOne(queryWrapper);
        // 4. 用户不存在或被封号
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        ThrowUtils.throwIf(UserRoleEnum.BAN.getValue().equals(user.getUserRole()),
                ErrorCode.FORBIDDEN_ERROR, "该用户已被封号");
        // 5. 返回用户实体
        return user;
    }

    /**
     * 用户登录
     * 验证用户登录信息，记录登录状态，返回脱敏的用户视图对象
     * 该方法会在会话中记录用户的登录状态
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求对象，用于存储会话信息
     * @return 脱敏后的用户视图对象
     * @throws BusinessException 登录失败时抛出异常
     */
    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 调用登录验证方法获取用户实体
        User user = userLoginVerify(userAccount, userPassword);
        // 4. 转换为视图对象
        UserVO userVO = toUserVO(user);
        // 5. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);
        // 6. 返回用户视图对象
        return userVO;
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
        UserVO userVO = (UserVO) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userVO == null || userVO.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2. 从数据库中查询最新的用户信息
        User user = this.getById(userVO.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    /**
     * 将用户实体转换为视图对象
     * 移除敏感信息，如密码等，返回可安全传输的用户视图对象
     * 保留用户ID、账号、名称、头像、角色、简介、创建时间和更新时间
     *
     * @param user 原始用户对象
     * @return 脱敏后的用户视图对象
     */
    @Override
    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUserAccount(user.getUserAccount());
        userVO.setUserName(user.getUserName());
        userVO.setUserAvatar(user.getUserAvatar());
        userVO.setUserRole(user.getUserRole());
        userVO.setUserProfile(user.getUserProfile());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
        return userVO;
    }

    /**
     * 获取加密密码
     * 使用MD5算法对密码进行加盐加密
     *
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    private String getEncryptPassword(String userPassword) {
        // 定义盐值，提高密码安全性
        final String SALT = "elwg";
        // 使用MD5加密，将盐值添加到密码前后
        return DigestUtils.md5DigestAsHex((SALT + userPassword + SALT).getBytes());
    }

    /**
     * 对密码进行加密
     * 使用系统的加密算法对密码进行加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    @Override
    public String encryptPassword(String password) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR, "密码不能为空");
        return getEncryptPassword(password);
    }

    /**
     * 用户退出
     * 清除用户登录状态
     *
     * @param request HTTP请求对象，用于清除会话信息
     * @return 退出结果，true 表示成功，false 表示失败
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            return false;
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏后的用户信息
     * 根据ID获取用户信息，并进行脱敏处理
     *
     * @param id 用户ID
     * @return 脱敏后的用户视图对象
     */
    @Override
    public UserVO getUser(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return toUserVO(user);
    }

    /**
     * 获取用户详细信息
     * 根据ID获取用户详细信息，并进行脱敏处理
     *
     * @param id 用户ID
     * @return 用户详细视图对象
     */
    @Override
    public UserDetailVO getUserDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return toUserDetailVO(user);
    }

    /**
     * 将用户实体转换为详细视图对象
     * 移除用户敏感信息，返回包含更多信息的用户详细视图对象
     *
     * @param user 原始用户对象
     * @return 用户详细视图对象
     */
    @Override
    public UserDetailVO toUserDetailVO(User user) {
        if (user == null) {
            return null;
        }

        // 创建新的用户详细视图对象
        UserDetailVO userDetailVO = new UserDetailVO();

        // 先设置基本字段（与 UserVO 相同的字段）
        userDetailVO.setId(user.getId());
        userDetailVO.setUserAccount(user.getUserAccount());
        userDetailVO.setUserName(user.getUserName());
        userDetailVO.setUserAvatar(user.getUserAvatar());
        userDetailVO.setUserProfile(user.getUserProfile());
        userDetailVO.setUserRole(user.getUserRole());
        userDetailVO.setCreateTime(user.getCreateTime());
        userDetailVO.setUpdateTime(user.getUpdateTime());

        // 设置详细字段
        userDetailVO.setEditTime(user.getEditTime());

        return userDetailVO;
    }

    /**
     * 获取用户列表（分页）
     * 根据查询条件获取用户列表，并进行脱敏处理
     *
     * @param userQueryRequest 用户查询请求
     * @return 分页用户列表（脱敏）
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        // 限制分页大小，防止查询过多数据
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小必须大于0");
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "分页大小不能超过50");
        ThrowUtils.throwIf(current <= 0, ErrorCode.PARAMS_ERROR, "当前页码必须大于0");

        // 构建查询条件
        QueryWrapper<User> queryWrapper = getQueryWrapper(userQueryRequest);

        // 创建分页对象
        Page<User> page = new Page<>(current, pageSize);
        // 设置是否查询总记录数
        page.setSearchCount(true);
        // 设置是否优化计数SQL
        page.setOptimizeCountSql(true);

        // 执行分页查询
        Page<User> userPage = this.page(page, queryWrapper);

        // 将 User 列表转换为 UserVO 列表
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
        userVOPage.setRecords(userVOList);

        // 复制分页相关属性
        userVOPage.setPages(userPage.getPages());
        userVOPage.setCountId(userPage.countId());
        userVOPage.setMaxLimit(userPage.maxLimit());
        userVOPage.setSearchCount(userPage.searchCount());
        userVOPage.setOptimizeCountSql(userPage.optimizeCountSql());
        userVOPage.setOptimizeJoinOfCountSql(userPage.optimizeJoinOfCountSql());

        return userVOPage;
    }

    /**
     * 根据条件构建用户查询条件
     * 将 UserQueryRequest 转换为 QueryWrapper
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件包装器
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (userQueryRequest == null) {
            // 默认排序为创建时间降序
            queryWrapper.orderByDesc("createTime");
            return queryWrapper;
        }

        // 提取查询条件
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        Date createTimeStart = userQueryRequest.getCreateTimeStart();
        Date createTimeEnd = userQueryRequest.getCreateTimeEnd();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 根据传入的参数构建查询条件
        // 1. ID 精确查询
        queryWrapper.eq(id != null && id > 0, "id", id);

        // 2. 用户账号模糊查询（包含）
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);

        // 3. 用户名模糊查询（包含）
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);

        // 4. 用户角色精确查询
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);

        // 5. 创建时间范围查询
        if (createTimeStart != null && createTimeEnd != null) {
            // 确保开始时间不晚于结束时间
            if (createTimeStart.after(createTimeEnd)) {
                Date temp = createTimeStart;
                createTimeStart = createTimeEnd;
                createTimeEnd = temp;
            }
            queryWrapper.between("createTime", createTimeStart, createTimeEnd);
        } else {
            // 如果只有开始时间或结束时间，则分别处理
            queryWrapper.ge(createTimeStart != null, "createTime", createTimeStart);
            queryWrapper.le(createTimeEnd != null, "createTime", createTimeEnd);
        }

        // 6. 排除已删除的用户
        queryWrapper.eq("isDelete", 0);

        // 7. 排序处理
        // 默认按创建时间降序排序
        queryWrapper.orderByDesc("createTime");

        // 如果有指定排序字段，则按指定字段排序
        if (StrUtil.isNotBlank(sortField)) {
            // 验证排序字段是否合法（防止SQL注入）
            boolean isValidField = checkValidSortField(sortField);
            if (isValidField) {
                boolean isAsc = "ascend".equals(sortOrder);
                queryWrapper.orderBy(true, isAsc, sortField);
            } else {
                log.warn("非法排序字段: {}", sortField);
            }
        }

        return queryWrapper;
    }

    /**
     * 检查排序字段是否合法
     * 防止SQL注入攻击
     *
     * @param sortField 排序字段
     * @return 是否合法
     */
    private boolean checkValidSortField(String sortField) {
        // 定义允许的排序字段列表
        String[] validFields = {
            "id", "userAccount", "userName", "userRole", "userProfile",
            "createTime", "updateTime", "editTime"
        };

        // 检查排序字段是否在允许的列表中
        for (String validField : validFields) {
            if (validField.equals(sortField)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证用户注册参数
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     */
    private void validateUserRegister(String userAccount, String userPassword, String checkPassword) {
        // 参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");

        // 账号长度不能小于4
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账号长度不能小于4");

        // 密码长度不能小于6
        ThrowUtils.throwIf(userPassword.length() < 6,
                ErrorCode.PARAMS_ERROR, "用户密码长度不能小于6");

        // 两次输入的密码必须一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    }

    /**
     * 验证用户创建参数
     *
     * @param userCreateRequest 用户创建请求
     * @param currentUser       当前操作的用户（用于权限检查）
     */
    private void validateUserCreate(UserCreateRequest userCreateRequest, User currentUser) {
        ThrowUtils.throwIf(userCreateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        String userAccount = userCreateRequest.getUserAccount();
        String userPassword = userCreateRequest.getUserPassword();
        String checkPassword = userCreateRequest.getCheckPassword();
        String userRole = userCreateRequest.getUserRole();

        // 基本参数校验
        validateUserRegister(userAccount, userPassword, checkPassword);

        // 校验用户角色是否合法
        if (StrUtil.isNotBlank(userRole)) {
            validateUserRole(userRole, currentUser);
        }
    }

    /**
     * 验证用户更新参数
     *
     * @param userUpdateRequest 用户更新请求
     * @param currentUser       当前操作的用户（用于权限检查）
     * @param oldUser           被更新的用户
     * @param adminCount        管理员数量（用于检查是否可以降级管理员）
     */
    private void validateUserUpdate(UserUpdateRequest userUpdateRequest, User currentUser, User oldUser, long adminCount) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 校验用户角色是否合法
        String userRole = userUpdateRequest.getUserRole();
        if (StrUtil.isNotBlank(userRole)) {
            UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色不合法");

            // 如果非管理员尝试将用户角色改为管理员，拒绝操作
            if (UserRoleEnum.ADMIN.getValue().equals(userRole) &&
                    !UserRoleEnum.ADMIN.getValue().equals(currentUser.getUserRole())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权将用户提升为管理员");
            }

            // 如果尝试将管理员降级，需要确保至少还有一个管理员
            if (UserRoleEnum.ADMIN.getValue().equals(oldUser.getUserRole()) &&
                    !UserRoleEnum.ADMIN.getValue().equals(userRole)) {
                // 使用传入的管理员数量进行检查
                ThrowUtils.throwIf(adminCount <= 1, ErrorCode.OPERATION_ERROR, "系统至少需要保留一个管理员");
            }
        }

        // 校验密码
        String userPassword = userUpdateRequest.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            ThrowUtils.throwIf(userPassword.length() < 6, ErrorCode.PARAMS_ERROR, "密码长度不能小于6");
        }
    }

    /**
     * 验证用户删除参数
     *
     * @param userId      要删除的用户ID
     * @param currentUser 当前操作的用户（用于权限检查）
     * @param userToDelete 要删除的用户
     */
    private void validateUserDelete(Long userId, User currentUser, User userToDelete) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(userToDelete == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 不允许删除管理员
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.getValue().equals(userToDelete.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "不允许删除管理员");

        // 不允许删除自己
        ThrowUtils.throwIf(userId.equals(currentUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "不允许删除当前登录用户");
    }

    /**
     * 验证用户个人资料更新参数
     *
     * @param profileUpdateRequest 个人资料更新请求
     * @param currentUser          当前用户
     * @param accountExists        账号是否已存在（用于检查账号是否可用）
     */
    private void validateProfileUpdate(UserProfileUpdateRequest profileUpdateRequest, User currentUser, boolean accountExists) {
        ThrowUtils.throwIf(profileUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 如果要更新账号，需要进行特殊处理
        String userAccount = profileUpdateRequest.getUserAccount();
        if (StrUtil.isNotBlank(userAccount)) {
            // 账号长度校验
            ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号长度不能小于4");

            // 如果账号发生变化，需要检查新账号是否已存在
            if (!userAccount.equals(currentUser.getUserAccount())) {
                // 使用传入的账号存在状态进行检查
                ThrowUtils.throwIf(accountExists, ErrorCode.PARAMS_ERROR, "账号已存在");
            }
        }
    }

    /**
     * 验证用户密码更新参数
     *
     * @param passwordUpdateRequest 密码更新请求
     */
    private void validatePasswordUpdate(UserPasswordUpdateRequest passwordUpdateRequest) {
        ThrowUtils.throwIf(passwordUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        String oldPassword = passwordUpdateRequest.getOldPassword();
        String newPassword = passwordUpdateRequest.getNewPassword();
        String checkPassword = passwordUpdateRequest.getCheckPassword();

        ThrowUtils.throwIf(StrUtil.hasBlank(oldPassword, newPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "密码参数不能为空");

        // 校验新密码长度
        ThrowUtils.throwIf(newPassword.length() < 6, ErrorCode.PARAMS_ERROR, "新密码长度不能小于6");

        // 校验两次输入的新密码是否一致
        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
    }

    /**
     * 验证用户角色是否合法
     *
     * @param userRole    用户角色
     * @param currentUser 当前操作的用户（用于权限检查）
     * @return 是否合法
     */
    private boolean validateUserRole(String userRole, User currentUser) {
        if (StrUtil.isBlank(userRole)) {
            return true;
        }

        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(userRole);
        ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色不合法");

        // 如果非管理员尝试创建管理员账号，拒绝操作
        if (UserRoleEnum.ADMIN.getValue().equals(userRole) &&
                !UserRoleEnum.ADMIN.getValue().equals(currentUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权创建管理员账号");
        }

        return true;
    }

    /**
     * 物理删除用户
     * 完全从数据库中删除用户记录，而不是逻辑删除
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    @Override
    public boolean physicalDeleteUser(Long id) {
        if (id == null || id <= 0) {
            log.error("物理删除用户失败: 用户ID不合法 {}", id);
            return false;
        }

        // 检查用户是否存在（包括逻辑删除的用户）
        // 使用自定义的Mapper方法查询用户，包括已被逻辑删除的用户
        User user = baseMapper.selectByIdIgnoreLogicDelete(id);

        if (user == null) {
            log.warn("物理删除用户失败: 用户不存在 {}", id);
            return false; // 如果用户不存在，返回删除失败，而不是抛出异常
        }

        // 记录要删除的用户信息，便于调试
        log.info("尝试物理删除用户: ID={}, 账号={}, 逻辑删除状态={}",
                id, user.getUserAccount(), user.getIsDelete());

        // 使用自定义的物理删除方法，绕过MyBatis-Plus的逻辑删除机制
        try {
            // 使用自定义的物理删除方法
            int result = baseMapper.physicalDeleteById(id);

            if (result > 0) {
                log.info("物理删除用户成功: {}", id);
                return true;
            } else {
                log.warn("使用自定义方法删除失败，尝试使用原生方式删除");
                // 如果删除失败，尝试使用原生的SQL执行删除
                result = baseMapper.delete(
                    new QueryWrapper<User>().eq("id", id)
                        .last("LIMIT 1") // 限制只删除一条记录，增加安全性
                );

                if (result > 0) {
                    log.info("使用QueryWrapper物理删除用户成功: {}", id);
                    return true;
                } else {
                    log.error("物理删除用户失败: {}", id);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("物理删除用户异常: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户个人资料
     * 允许用户更新自己的个人资料，包括账号、用户名、头像和简介
     *
     * @param profileUpdateRequest 个人资料更新请求
     * @param request HTTP请求对象
     * @return 更新后的用户视图对象
     */
    @Override
    public UserVO updateUserProfile(UserProfileUpdateRequest profileUpdateRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 检查账号是否已存在
        boolean accountExists = false;
        String userAccount = profileUpdateRequest.getUserAccount();
        if (StrUtil.isNotBlank(userAccount) && !userAccount.equals(loginUser.getUserAccount())) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            queryWrapper.eq("isDelete", 0); // 只检查未被逻辑删除的账号
            accountExists = this.count(queryWrapper) > 0;
        }

        // 验证参数
        validateProfileUpdate(profileUpdateRequest, loginUser, accountExists);

        // 创建新的用户对象并设置ID
        User user = new User();
        user.setId(loginUser.getId());

        // 如果要更新账号，需要进行特殊处理
        if (StrUtil.isNotBlank(userAccount) && !userAccount.equals(loginUser.getUserAccount())) {
            // 设置新账号
            user.setUserAccount(userAccount);
        }

        // 设置要更新的字段
        if (StrUtil.isNotBlank(profileUpdateRequest.getUserName())) {
            user.setUserName(profileUpdateRequest.getUserName());
        }

        if (StrUtil.isNotBlank(profileUpdateRequest.getUserAvatar())) {
            user.setUserAvatar(profileUpdateRequest.getUserAvatar());
        }

        if (StrUtil.isNotBlank(profileUpdateRequest.getUserProfile())) {
            user.setUserProfile(profileUpdateRequest.getUserProfile());
        }

        // 设置编辑时间
        user.setEditTime(new Date());

        // 更新用户信息
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");

        // 获取更新后的用户信息
        User updatedUser = this.getById(loginUser.getId());
        UserVO userVO = toUserVO(updatedUser);

        // 更新会话中的用户信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);

        return userVO;
    }

    /**
     * 管理员更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @param request HTTP请求对象
     * @return 是否更新成功
     */
    @Override
    public boolean adminUpdateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 判断是否存在
        long id = userUpdateRequest.getId();
        User oldUser = this.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 查询管理员数量（用于检查是否可以降级管理员）
        long adminCount = 0;
        String userRole = userUpdateRequest.getUserRole();
        if (StrUtil.isNotBlank(userRole) &&
                UserRoleEnum.ADMIN.getValue().equals(oldUser.getUserRole()) &&
                !UserRoleEnum.ADMIN.getValue().equals(userRole)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userRole", UserRoleEnum.ADMIN.getValue());
            adminCount = this.count(queryWrapper);
        }

        // 验证参数
        validateUserUpdate(userUpdateRequest, loginUser, oldUser, adminCount);

        // 创建新的用户对象并复制属性
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);

        // 设置编辑时间
        user.setEditTime(new Date());

        // 如果提供了新密码，需要对密码进行加密
        String userPassword = userUpdateRequest.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            // 调用服务层的方法对密码进行加密
            user.setUserPassword(encryptPassword(userPassword));
        } else {
            // 如果没有提供密码，不更新密码字段
            user.setUserPassword(null);
        }

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return true;
    }

    /**
     * 验证并创建新用户
     *
     * @param userCreateRequest 用户创建请求
     * @param request HTTP请求对象
     * @return 新用户ID
     */
    @Override
    public Long createUserByAdmin(UserCreateRequest userCreateRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 验证参数
        validateUserCreate(userCreateRequest, loginUser);

        String userAccount = userCreateRequest.getUserAccount();
        String userPassword = userCreateRequest.getUserPassword();
        String checkPassword = userCreateRequest.getCheckPassword();
        String userName = userCreateRequest.getUserName();
        String userRole = userCreateRequest.getUserRole();

        // 调用注册方法创建用户
        long userId = userRegister(userAccount, userPassword, checkPassword);

        // 创建用户后直接更新附加信息，避免二次查询
        User user = new User();
        user.setId(userId);

        // 设置用户名，如果未提供则使用默认值
        if (StrUtil.isNotBlank(userName)) {
            user.setUserName(userName);
        }

        // 设置用户角色，如果未提供则使用默认值
        if (StrUtil.isNotBlank(userRole)) {
            user.setUserRole(userRole);
        }

        // 设置用户头像
        if (StrUtil.isNotBlank(userCreateRequest.getUserAvatar())) {
            user.setUserAvatar(userCreateRequest.getUserAvatar());
        }

        // 设置用户简介
        if (StrUtil.isNotBlank(userCreateRequest.getUserProfile())) {
            user.setUserProfile(userCreateRequest.getUserProfile());
        }

        // 设置编辑时间
        user.setEditTime(new Date());

        // 更新用户信息
        boolean updateResult = this.updateById(user);
        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "用户信息更新失败");

        return userId;
    }

    /**
     * 更新用户密码
     *
     * @param passwordUpdateRequest 密码更新请求
     * @param request HTTP请求对象
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserPassword(UserPasswordUpdateRequest passwordUpdateRequest, HttpServletRequest request) {
        // 验证参数
        validatePasswordUpdate(passwordUpdateRequest);

        String oldPassword = passwordUpdateRequest.getOldPassword();
        String newPassword = passwordUpdateRequest.getNewPassword();

        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 验证旧密码是否正确
        // 获取加密后的旧密码
        String encryptedOldPassword = encryptPassword(oldPassword);

        // 比较加密后的旧密码与数据库中的密码是否一致
        ThrowUtils.throwIf(!encryptedOldPassword.equals(loginUser.getUserPassword()),
                ErrorCode.PARAMS_ERROR, "旧密码不正确");

        // 加密新密码
        String encryptedNewPassword = encryptPassword(newPassword);

        // 更新密码
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(encryptedNewPassword);
        user.setEditTime(new Date());

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "密码更新失败");

        return true;
    }

    /**
     * 删除用户（管理员操作）
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求对象
     * @return 是否删除成功
     */
    @Override
    public boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "请求参数为空");

        long id = deleteRequest.getId();
        // 判断是否存在
        User oldUser = this.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 验证删除操作
        validateUserDelete(id, loginUser, oldUser);

        // 执行删除操作
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除失败");

        return true;
    }

    /**
     * 获取所有用户信息（管理员操作）
     *
     * @return 所有用户信息列表
     */
    @Override
    public List<UserVO> listAllUsers() {
        // 创建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("createTime"); // 按创建时间降序排序

        // 查询总数
        long count = this.count(queryWrapper);

        // 如果数据量过大，建议使用分页接口
        if (count > 100) {
            log.warn("用户数量过多，建议使用分页接口，当前用户数量: {}", count);

            // 创建分页对象，设置每页最大数量为100
            Page<User> page = new Page<>(1, 100);
            // 不需要再次查询总数，使用新API
            page.setSearchCount(false);

            // 执行分页查询
            Page<User> userPage = this.page(page, queryWrapper);

            // 脱敏处理
            return userPage.getRecords().stream()
                    .map(this::toUserVO)
                    .collect(Collectors.toList());
        }

        // 如果数据量不大，直接查询所有
        List<User> userList = this.list(queryWrapper);

        // 脱敏处理
        return userList.stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
    }
}