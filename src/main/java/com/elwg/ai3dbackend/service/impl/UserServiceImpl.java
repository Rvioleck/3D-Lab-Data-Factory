package com.elwg.ai3dbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.service.UserService;
import com.elwg.ai3dbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 35245
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-04-13 16:42:37
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




