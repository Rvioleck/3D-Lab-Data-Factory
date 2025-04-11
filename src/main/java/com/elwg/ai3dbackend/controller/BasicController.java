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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基础控制器
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
public class BasicController {

    /**
     * 问候接口
     *
     * @param name 用户名
     * @return 问候消息
     */
    @RequestMapping("/hello")
    @ResponseBody
    public BaseResponse<String> hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        String greeting = "Hello " + name;
        return ResultUtils.success(greeting);
    }

    /**
     * 获取用户信息接口
     *
     * @return 用户信息
     */
    @RequestMapping("/user")
    @ResponseBody
    public BaseResponse<User> user() {
        User user = new User();
        user.setName("theonefx");
        user.setAge(666);
        return ResultUtils.success(user);
    }

    /**
     * 保存用户信息接口
     *
     * @param u 用户对象
     * @return 保存结果
     */
    @RequestMapping("/save_user")
    @ResponseBody
    public BaseResponse<String> saveUser(User u) {
        if (u == null || u.getName() == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户参数不能为空");
        }
        String result = "用户将被保存: 姓名=" + u.getName() + ", 年龄=" + u.getAge();
        return ResultUtils.success(result);
    }

    /**
     * HTML页面跳转
     *
     * @return 页面路径
     */
    @RequestMapping("/html")
    public String html() {
        return "index.html";
    }

    /**
     * 用户参数解析
     *
     * @param name 用户名
     * @param age 年龄
     * @param user 用户对象
     */
    @ModelAttribute
    public void parseUser(@RequestParam(name = "name", defaultValue = "unknown user") String name,
                         @RequestParam(name = "age", defaultValue = "12") Integer age,
                         User user) {
        user.setName("zhangsan");
        user.setAge(18);
    }
}
