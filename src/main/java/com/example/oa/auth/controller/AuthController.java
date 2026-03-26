package com.example.oa.auth.controller;

import com.example.oa.auth.dto.ChangePwdRequest;
import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.auth.dto.ProfileResponse;
import com.example.oa.common.response.ApiResponse;
import com.example.oa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户登录：校验用户名密码并签发 token。
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@RequestHeader("Authorization") String token) {
        LoginResponse response = sysUserService.getCurrentUser(token);
        return ApiResponse.success(response);
    }

    /**
     * 退出登录：当前阶段仅做 token 合法性校验。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        sysUserService.logout(token);
        return ApiResponse.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePwd(@Valid @RequestBody ChangePwdRequest request, @RequestHeader("Authorization") String token) {
        sysUserService.changePassword(request, token);
        return ApiResponse.success();
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(@RequestHeader("Authorization") String token) {
        ProfileResponse response = sysUserService.getProfile(token);
        return ApiResponse.success(response);
    }
}