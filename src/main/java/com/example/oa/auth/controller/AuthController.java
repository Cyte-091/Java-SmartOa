package com.example.oa.auth.controller;

import com.example.oa.common.response.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", 1L);
        data.put("username", "admin");
        data.put("displayName", "System Admin");
        data.put("roles", Arrays.asList("ADMIN"));
        return ApiResponse.success(data);
    }
}