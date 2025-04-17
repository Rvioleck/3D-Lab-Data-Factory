package com.elwg.ai3dbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.DeleteRequest;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.model.dto.UserCreateRequest;
import com.elwg.ai3dbackend.model.dto.UserLoginRequest;
import com.elwg.ai3dbackend.model.dto.UserQueryRequest;
import com.elwg.ai3dbackend.model.dto.UserRegisterRequest;
import com.elwg.ai3dbackend.model.dto.UserUpdateRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.enums.UserRoleEnum;
import com.elwg.ai3dbackend.model.vo.UserDetailVO;
import com.elwg.ai3dbackend.model.vo.UserVO;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Date;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 * <p>
 * 提供用户相关的API接口，包括用户注册、登录和获取用户信息等功能。
 * 所有接口都返回统一的BaseResponse格式，包含状态码、消息和数据。
 * </p>
 *
 * @author 方一舟
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户接口", description = "提供用户注册、登录和获取用户信息等功能")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * <p>
     * 接收用户注册请求，验证参数后调用服务层进行用户注册。注册流程包括：
     * 1. 验证请求参数的完整性和有效性
     * 2. 检查账号是否已存在
     * 3. 验证密码和确认密码是否一致
     * 4. 对密码进行加密处理
     * 5. 创建新用户并保存到数据库
     * </p>
     *
     * @param userRegisterRequest 用户注册请求体，包含账号、密码和确认密码
     * @return 新用户id，包装在BaseResponse中
     * @throws BusinessException 参数错误或账号已存在时抛出业务异常
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户，需要提供账号、密码和确认密码")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * <p>
     * 接收用户登录请求，验证参数后调用服务层完成登录，并在会话中记录登录状态。登录流程包括：
     * 1. 验证请求参数的完整性
     * 2. 根据账号查找用户
     * 3. 验证密码是否正确
     * 4. 生成脱敏的用户信息（不包含敏感字段如密码）
     * 5. 在会话中记录用户登录状态
     * </p>
     *
     * @param userLoginRequest 用户登录请求体，包含账号和密码
     * @param request          HTTP请求对象，用于存储会话信息
     * @return 包含脱敏后的用户信息的响应对象，包括用户ID、账号、名称等非敏感信息
     * @throws BusinessException 参数错误、账号不存在或密码错误时抛出业务异常
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "验证用户身份并创建会话，返回脱敏的用户信息")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                          HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 从会话中获取当前登录用户，并返回脱敏后的用户信息。该接口用于前端获取当前登录状态和用户基本信息，
     * 常用于页面初始化、导航栏显示用户信息等场景。如果用户未登录，将抛出业务异常。
     * </p>
     * <p>
     * 返回的用户信息经过脱敏处理，不包含敏感字段如密码等。
     * </p>
     *
     * @param request HTTP请求对象，用于获取会话中的用户信息
     * @return 包含脱敏后的用户信息的响应对象，包括用户ID、账号、名称等非敏感信息
     * @throws BusinessException 用户未登录时抛出业务异常
     */
    @PostMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户信息", notes = "从当前会话中获取登录用户信息，用户未登录时会返回错误")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        // 转换为 UserVO
        UserVO userVO = userService.toUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户退出
     * <p>
     * 清除用户的登录状态，实现用户退出功能。该接口会清除会话中的用户信息，
     * 使用户返回未登录状态。如果用户已经处于未登录状态，也会返回成功。
     * </p>
     *
     * @param request HTTP请求对象，用于清除会话中的用户信息
     * @return 包含退出结果的响应对象，成功时返回 true
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户退出", notes = "清除用户登录状态，实现退出功能")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于创建新用户。
     * 创建成功后返回新用户的ID。
     * </p>
     *
     * @param userCreateRequest 用户创建请求体
     * @param request HTTP请求对象
     * @return 包含新用户ID的响应对象
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建用户", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createUser(@RequestBody UserCreateRequest userCreateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userCreateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        String userAccount = userCreateRequest.getUserAccount();
        String userPassword = userCreateRequest.getUserPassword();
        String checkPassword = userCreateRequest.getCheckPassword();
        String userName = userCreateRequest.getUserName();
        String userRole = userCreateRequest.getUserRole();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "用户账号不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号长度不能小于4");
        ThrowUtils.throwIf(StrUtil.isBlank(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码长度不能小于8");
        ThrowUtils.throwIf(StrUtil.isBlank(checkPassword), ErrorCode.PARAMS_ERROR, "确认密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 校验用户角色是否合法
        if (StrUtil.isNotBlank(userRole)) {
            UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色不合法");

            // 如果非管理员尝试创建管理员账号，拒绝操作
            User loginUser = userService.getLoginUser(request);
            if (UserRoleEnum.ADMIN.getValue().equals(userRole) && !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权创建管理员账号");
            }
        }

        // 调用注册方法创建用户
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);

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
        boolean updateResult = userService.updateById(user);
        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "用户信息更新失败");

        return ResultUtils.success(userId);
    }

    /**
     * 删除用户（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于删除指定用户。
     * 删除成功后返回 true。
     * </p>
     *
     * @param deleteRequest 删除请求体，包含要删除的用户ID
     * @param request HTTP请求对象
     * @return 包含删除结果的响应对象
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        long id = deleteRequest.getId();
        // 判断是否存在
        User oldUser = userService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 不允许删除管理员
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.getValue().equals(oldUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "不允许删除管理员");

        // 不允许删除自己
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(id == loginUser.getId(), ErrorCode.NO_AUTH_ERROR, "不允许删除当前登录用户");

        // 执行删除操作
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除失败");

        return ResultUtils.success(true);
    }

    /**
     * 更新用户（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于更新指定用户的信息。
     * 更新成功后返回 true。
     * </p>
     *
     * @param userUpdateRequest 用户更新请求体
     * @param request HTTP请求对象
     * @return 包含更新结果的响应对象
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新用户", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 判断是否存在
        long id = userUpdateRequest.getId();
        User oldUser = userService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 校验用户角色是否合法
        String userRole = userUpdateRequest.getUserRole();
        if (StrUtil.isNotBlank(userRole)) {
            UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色不合法");

            // 如果非管理员尝试将用户角色改为管理员，拒绝操作
            if (UserRoleEnum.ADMIN.getValue().equals(userRole) && !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权将用户提升为管理员");
            }

            // 如果尝试将管理员降级，需要确保至少还有一个管理员
            if (UserRoleEnum.ADMIN.getValue().equals(oldUser.getUserRole()) && !UserRoleEnum.ADMIN.getValue().equals(userRole)) {
                // 查询管理员数量
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userRole", UserRoleEnum.ADMIN.getValue());
                long adminCount = userService.count(queryWrapper);
                ThrowUtils.throwIf(adminCount <= 1, ErrorCode.OPERATION_ERROR, "系统至少需要保留一个管理员");
            }
        }

        // 校验密码
        String userPassword = userUpdateRequest.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
        }

        // 创建新的用户对象并复制属性
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);

        // 设置编辑时间
        user.setEditTime(new Date());

        // 如果提供了新密码，需要对密码进行加密
        if (StrUtil.isNotBlank(userPassword)) {
            // 调用服务层的方法对密码进行加密
            user.setUserPassword(userService.encryptPassword(userPassword));
        } else {
            // 如果没有提供密码，不更新密码字段
            user.setUserPassword(null);
        }

        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据ID获取用户基本信息（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于获取指定用户的基本信息。
     * 返回的用户信息已经进行脱敏处理，不包含敏感字段如密码等。
     * </p>
     *
     * @param id 用户ID
     * @return 包含用户基本信息的响应对象
     */
    @PostMapping("/get")
    @ApiOperation(value = "根据ID获取用户基本信息", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserById(@RequestBody Long id) {
        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID不合法");

        // 查询用户
        UserVO userVO = userService.getUser(id);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return ResultUtils.success(userVO);
    }

    /**
     * 根据ID获取用户详细信息（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于获取指定用户的详细信息。
     * 返回的用户信息已经进行脱敏处理，不包含敏感字段如密码等，
     * 但包含更多的用户详细信息。
     * </p>
     *
     * @param id 用户ID
     * @return 包含用户详细信息的响应对象
     */
    @PostMapping("/detail")
    @ApiOperation(value = "根据ID获取用户详细信息", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserDetailVO> getUserDetailById(@RequestBody Long id) {
        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID不合法");

        // 查询用户详细信息
        UserDetailVO userDetailVO = userService.getUserDetail(id);
        ThrowUtils.throwIf(userDetailVO == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        return ResultUtils.success(userDetailVO);
    }

    /**
     * 分页获取用户列表（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于分页获取用户列表。
     * 支持根据多种条件进行过滤和排序。
     * 返回的用户信息已经进行脱敏处理，不包含敏感字段如密码等。
     * </p>
     *
     * @param userQueryRequest 用户查询请求体
     * @return 包含分页用户列表的响应对象
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取用户列表", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 获取分页参数
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        // 限制分页大小，防止查询过多数据
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小必须大于0");
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "分页大小不能超过50");
        ThrowUtils.throwIf(current <= 0, ErrorCode.PARAMS_ERROR, "当前页码必须大于0");

        // 校验用户角色是否合法
        String userRole = userQueryRequest.getUserRole();
        if (StrUtil.isNotBlank(userRole)) {
            UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色不合法");
        }

        // 获取分页用户列表
        Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);

        // 日志记录分页信息，方便调试
        log.info("分页查询结果 - 总条数: {}, 总页数: {}, 当前页: {}, 每页条数: {}",
                userVOPage.getTotal(), userVOPage.getPages(), userVOPage.getCurrent(), userVOPage.getSize());

        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取所有用户信息（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于获取系统中所有用户的信息。
     * 返回的用户信息已经进行脱敏处理，不包含敏感字段如密码等。
     * </p>
     *
     * @return 包含所有用户信息的响应对象
     */
    @PostMapping("/list")
    @ApiOperation(value = "获取所有用户信息", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserVO>> listUsers() {
        // 使用分页插件获取所有用户，防止数据量过大
        // 创建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("createTime"); // 按创建时间降序排序

        // 查询总数
        long count = userService.count(queryWrapper);

        // 如果数据量过大，建议使用分页接口
        if (count > 100) {
            log.warn("用户数量过多，建议使用分页接口，当前用户数量: {}", count);

            // 创建分页对象，设置每页最大数量为100
            Page<User> page = new Page<>(1, 100);
            // 不需要再次查询总数，使用新API
            page.setSearchCount(false);

            // 执行分页查询
            Page<User> userPage = userService.page(page, queryWrapper);

            // 脱敏处理
            List<UserVO> result = userPage.getRecords().stream()
                    .map(user -> userService.toUserVO(user))
                    .collect(Collectors.toList());

            return ResultUtils.success(result);
        }

        // 如果数据量不大，直接查询所有
        List<User> userList = userService.list(queryWrapper);

        // 脱敏处理
        List<UserVO> result = userList.stream()
                .map(user -> userService.toUserVO(user))
                .collect(Collectors.toList());

        return ResultUtils.success(result);
    }
}
