package com.example.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa.auth.dto.request.ChangePwdRequest;
import com.example.oa.auth.dto.request.LoginRequest;
import com.example.oa.auth.dto.request.ResetPwdRequest;
import com.example.oa.auth.dto.request.UpdateStatusRequest;
import com.example.oa.auth.dto.response.LoginResponse;
import com.example.oa.auth.dto.response.ProfileResponse;
import com.example.oa.auth.dto.response.UserListResponse;
import com.example.oa.model.SysUser;

import java.util.List;

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

    void resetPwd(Long id, ResetPwdRequest request, String token);

    void updateStatus(Long id, UpdateStatusRequest request, String token);

    List<UserListResponse> getUserList(String tokenHeader);
}