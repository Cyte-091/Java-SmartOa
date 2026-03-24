package com.example.oa.controller;

import com.example.oa.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("status", "UP");
        data.put("service", "smart-oa-backend");
        return ApiResponse.success(data);
    }
}