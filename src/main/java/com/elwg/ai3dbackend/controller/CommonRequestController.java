package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.*;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用请求示例控制器
 * 用于演示通用请求和响应类的使用
 */
@RestController
@RequestMapping("/common")
public class CommonRequestController {

    /**
     * 删除操作示例
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 模拟删除操作
        boolean success = deleteRequest.getId() > 0;
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 分页查询示例
     *
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    @PostMapping("/list/page")
    public BaseResponse<PageResponse<User>> listByPage(@RequestBody PageRequest pageRequest) {
        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        
        // 参数校验
        if (current <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数错误");
        }
        
        // 模拟分页查询
        List<User> userList = new ArrayList<>();
        // 创建模拟数据
        for (int i = 0; i < pageSize; i++) {
            User user = new User();
            user.setName("用户" + (current * pageSize + i));
            user.setAge(20 + i);
            userList.add(user);
        }
        
        // 创建分页响应
        PageResponse<User> pageResponse = PageResponse.of(100, userList, current, pageSize);
        return ResultUtils.success(pageResponse);
    }
}
