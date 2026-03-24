package com.example.oa.rbac.controller;

import com.example.oa.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rbac")
public class MenuController {

    @GetMapping("/menus")
    public ApiResponse<List<Map<String, Object>>> menus(@RequestHeader(value = "Authorization", required = false) String authorization) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        if (authorization == null || authorization.trim().isEmpty()) {
            return ApiResponse.success(result);
        }

        Map<String, Object> dashboard = new HashMap<String, Object>();
        dashboard.put("name", "Dashboard");
        dashboard.put("path", "/");
        dashboard.put("component", "HomeView");
        result.add(dashboard);

        Map<String, Object> approval = new HashMap<String, Object>();
        approval.put("name", "Approval Center");
        approval.put("path", "/approval");
        approval.put("component", "Placeholder");
        result.add(approval);

        return ApiResponse.success(result);
    }
}