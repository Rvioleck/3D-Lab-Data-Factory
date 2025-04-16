package com.elwg.ai3dbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.model.dto.UserLoginRequest;
import com.elwg.ai3dbackend.model.dto.UserRegisterRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.LoginUserVO;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 *
 * @author 方一舟
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户接口")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * <p>
     * 接收用户注册请求，验证参数后调用服务层进行用户注册
     *
     * @param userRegisterRequest 用户注册请求体，包含账号、密码和确认密码
     * @return 新用户id，包装在BaseResponse中
     * @throws BusinessException 参数错误时抛出业务异常
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
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
     * 接收用户登录请求，验证参数后调用服务层完成登录，并在会话中记录登录状态
     *
     * @param userLoginRequest 用户登录请求体，包含账号和密码
     * @param request          HTTP请求对象，用于存储会话信息
     * @return 包含脱敏后的用户信息的响应对象
     * @throws BusinessException 参数错误或登录失败时抛出业务异常
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户信息
     * 从会话中获取当前登录用户，并返回脱敏后的用户信息
     *
     * @param request HTTP请求对象，用于获取会话中的用户信息
     * @return 包含脱敏后的用户信息的响应对象
     * @throws BusinessException 用户未登录时抛出业务异常
     */
    @PostMapping("/get/login")
    @ApiOperation("获取当前登录用户信息")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        // 先进行脱敏处理
        User safetyUser = userService.getSafetyUser(user);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(safetyUser, loginUserVO);
        return ResultUtils.success(loginUserVO);
    }

}
