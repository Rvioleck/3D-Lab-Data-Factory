/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.exception.ErrorCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 路径变量控制器
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
public class PathVariableController {

    /**
     * 获取用户角色信息
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色信息
     */
    @RequestMapping(value = "/user/{userId}/roles/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse<String> getLogin(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId) {
        if (userId == null || userId.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        if (roleId == null || roleId.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        }
        String result = "用户ID: " + userId + " 角色ID: " + roleId;
        return ResultUtils.success(result);
    }

    /**
     * 正则表达式路径变量示例
     *
     * @param regexp1 路径变量
     * @return 路径变量信息
     */
    @RequestMapping(value = "/javabeat/{regexp1:[a-z-]+}", method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse<String> getRegExp(@PathVariable("regexp1") String regexp1) {
        if (regexp1 == null || regexp1.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "路径变量不能为空");
        }
        String result = "路径变量值: " + regexp1;
        return ResultUtils.success(result);
    }
}
