package com.example.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa.auth.dto.ChangePwdRequest;
import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.auth.dto.ProfileResponse;
import com.example.oa.model.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 登录
     * @param request
     * @return
     */
    LoginResponse login(LoginRequest request);

    LoginResponse getCurrentUser(String token);

    /**
     * 退出登录。
     * 说明：JWT 是无状态的，严格退出通常需要黑名单机制。
     */
    void logout(String tokenHeader);

    void changePassword(ChangePwdRequest request, String token);

    ProfileResponse getProfile(String token);
}