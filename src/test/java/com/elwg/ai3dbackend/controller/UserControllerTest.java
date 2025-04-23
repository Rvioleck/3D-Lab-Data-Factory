package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.model.dto.UserProfileUpdateRequest;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.UserVO;
import com.elwg.ai3dbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 用户控制器测试类
 */
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockHttpServletRequest request;
    private User mockUser;
    private UserVO mockUserVO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();

        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUserAccount("testuser");
        mockUser.setUserName("Test User");
        mockUser.setUserAvatar("avatar.jpg");
        mockUser.setUserProfile("Test profile");
        mockUser.setUserRole("user");

        // 创建模拟用户VO
        mockUserVO = new UserVO();
        mockUserVO.setId(1L);
        mockUserVO.setUserAccount("testuser");
        mockUserVO.setUserName("Test User");
        mockUserVO.setUserAvatar("avatar.jpg");
        mockUserVO.setUserProfile("Test profile");
        mockUserVO.setUserRole("user");

        // 模拟用户服务的行为
        when(userService.getLoginUser(any())).thenReturn(mockUser);
        when(userService.getById(eq(1L))).thenReturn(mockUser);
        when(userService.toUserVO(any(User.class))).thenReturn(mockUserVO);
        when(userService.updateById(any(User.class))).thenReturn(true);
    }

    /**
     * 测试用户个人资料更新功能
     */
    @Test
    public void testUpdateUserProfile() {
        // 创建请求对象
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUserName("Updated Name");
        request.setUserProfile("Updated profile");

        // 执行更新操作
        var response = userController.updateUserProfile(request, this.request);

        // 验证结果
        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertNotNull(response.getData());

        // 验证服务方法被调用
        verify(userService, times(1)).getLoginUser(any());
        verify(userService, times(1)).updateById(any(User.class));
        verify(userService, times(1)).getById(eq(1L));
        verify(userService, times(1)).toUserVO(any(User.class));
    }

    /**
     * 测试用户更新账号功能
     */
    @Test
    public void testUpdateUserAccount() {
        // 创建请求对象
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUserAccount("newaccount");

        // 模拟服务方法行为 - 检查账号是否存在
        when(userService.count(any())).thenReturn(0L);

        // 执行更新操作
        var response = userController.updateUserProfile(request, this.request);

        // 验证结果
        assertNotNull(response);
        assertEquals(0, response.getCode());

        // 验证服务方法被调用
        verify(userService, times(1)).count(any());
        verify(userService, times(1)).updateById(any(User.class));
    }
}
