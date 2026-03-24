package com.example.oa.auth.controller;

import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.common.response.ApiResponse;
import com.example.oa.model.SysUser;
import com.example.oa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);

        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@RequestHeader("Authorization") String token) {
        LoginResponse response = sysUserService.getCurrentUser(token);

        return ApiResponse.success(response);
    }

}
