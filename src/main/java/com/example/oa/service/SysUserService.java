package com.example.oa.service;

import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.model.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author DELL
* @description 针对表【sys_user】的数据库操作Service
* @createDate 2026-03-23 18:10:41
*/
public interface SysUserService extends IService<SysUser> {

    LoginResponse login(LoginRequest request);

    LoginResponse getCurrentUser(String token);
}
