package com.elwg.ai3dbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.DeleteRequest;
import com.elwg.ai3dbackend.common.GetRequest;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.model.dto.UserCreateRequest;
import com.elwg.ai3dbackend.model.dto.UserLoginRequest;
import com.elwg.ai3dbackend.model.dto.UserPasswordUpdateRequest;
import com.elwg.ai3dbackend.model.dto.UserProfileUpdateRequest;
import com.elwg.ai3dbackend.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private FileStorageService fileStorageService;

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
        long userId = userService.createUserByAdmin(userCreateRequest, request);
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
        boolean result = userService.deleteUser(deleteRequest, request);
        return ResultUtils.success(result);
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
        boolean result = userService.adminUpdateUser(userUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取用户基本信息（仅管理员可访问）
     * <p>
     * 该接口只允许管理员访问，用于获取指定用户的基本信息。
     * 返回的用户信息已经进行脱敏处理，不包含敏感字段如密码等。
     * </p>
     *
     * @param getRequest 包含用户ID的获取请求对象
     * @return 包含用户基本信息的响应对象
     */
    @PostMapping("/get")
    @ApiOperation(value = "根据ID获取用户基本信息", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserById(@RequestBody GetRequest getRequest) {
        // 参数校验
        ThrowUtils.throwIf(getRequest == null || getRequest.getId() == null || getRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "用户ID不合法");

        // 查询用户
        UserVO userVO = userService.getUser(getRequest.getId());
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
     * @param getRequest 包含用户ID的获取请求对象
     * @return 包含用户详细信息的响应对象
     */
    @PostMapping("/detail")
    @ApiOperation(value = "根据ID获取用户详细信息", notes = "仅管理员可访问")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserDetailVO> getUserDetailById(@RequestBody GetRequest getRequest) {
        // 参数校验
        ThrowUtils.throwIf(getRequest == null || getRequest.getId() == null || getRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "用户ID不合法");

        // 查询用户详细信息
        UserDetailVO userDetailVO = userService.getUserDetail(getRequest.getId());
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
        List<UserVO> userVOList = userService.listAllUsers();
        return ResultUtils.success(userVOList);
    }

    /**
     * 更新当前用户个人资料
     * <p>
     * 该接口允许已登录用户更新自己的个人资料，包括账号、用户名、头像和简介。
     * 更新成功后返回更新后的用户信息。
     * </p>
     *
     * @param profileUpdateRequest 个人资料更新请求体
     * @param request HTTP请求对象
     * @return 包含更新后的用户信息的响应对象
     */
    @PostMapping("/profile/update")
    @ApiOperation(value = "更新个人资料", notes = "更新当前登录用户的个人资料")
    public BaseResponse<UserVO> updateUserProfile(@RequestBody UserProfileUpdateRequest profileUpdateRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(profileUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        UserVO userVO = userService.updateUserProfile(profileUpdateRequest, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 上传用户头像
     * <p>
     * 该接口允许已登录用户上传自己的头像图片。
     * 上传成功后返回头像URL。
     * </p>
     *
     * @param file 头像图片文件
     * @param request HTTP请求对象
     * @return 包含头像URL的响应对象
     */
    @PostMapping("/avatar/upload")
    @ApiOperation(value = "上传用户头像", notes = "上传当前登录用户的头像图片")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 获取当前登录用户 - 通过AuthCheck注解已经确保用户已登录
        User loginUser = userService.getLoginUser(request);

        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }

        // 校验文件大小
        long fileSize = file.getSize();
        // 限制文件大小为5MB
        final long FILE_SIZE_LIMIT = 5 * 1024 * 1024L;
        if (fileSize > FILE_SIZE_LIMIT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过限制");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        log.info("上传头像: 文件名={}, 类型={}, 大小={}", originalFilename, contentType, fileSize);

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误，仅支持图片");
        }

        try {
            // 生成文件路径
            String uuid = UUID.randomUUID().toString();
            String fileExtension = getFileExtension(originalFilename);
            String filePath = String.format("avatars/%s%s", uuid, fileExtension);

            // 上传文件
            fileStorageService.saveFile(filePath, file.getInputStream());

            // 获取文件URL
            String fileUrl = fileStorageService.getFileUrl(filePath);

            // 更新用户头像URL
            User user = new User();
            user.setId(loginUser.getId());
            user.setUserAvatar(fileUrl);
            user.setEditTime(new Date());
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "头像更新失败");

            // 更新会话中的用户信息
            User updatedUser = userService.getById(loginUser.getId());
            UserVO userVO = userService.toUserVO(updatedUser);
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);

            return ResultUtils.success(fileUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（包含点号）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return ".png"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 修改当前用户密码
     * <p>
     * 该接口允许已登录用户修改自己的密码。
     * 修改成功后返回true。
     * </p>
     *
     * @param passwordUpdateRequest 密码更新请求体
     * @param request HTTP请求对象
     * @return 包含修改结果的响应对象
     */
    @PostMapping("/password/update")
    @ApiOperation(value = "修改密码", notes = "修改当前登录用户的密码")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserPasswordUpdateRequest passwordUpdateRequest,
                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(passwordUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        boolean result = userService.updateUserPassword(passwordUpdateRequest, request);
        return ResultUtils.success(result);
    }
}
