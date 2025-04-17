package com.elwg.ai3dbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.mapper.UserMapper;
import com.elwg.ai3dbackend.model.dto.UserQueryRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.model.vo.UserDetailVO;
import com.elwg.ai3dbackend.model.vo.UserVO;
import com.elwg.ai3dbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
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
}