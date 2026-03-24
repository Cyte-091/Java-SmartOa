package com.example.oa.auth.controller;

import com.example.oa.auth.dto.LoginRequest;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.common.response.ApiResponse;
import com.example.oa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);

        return ApiResponse.success(response);
    }

}
