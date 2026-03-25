package com.example.oa.rbac.controller;

import com.example.oa.common.response.ApiResponse;
import com.example.oa.rbac.dto.MenuResponse;
import com.example.oa.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rbac")
public class MenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取当前登录用户可见菜单。
     * Controller 只负责接收参数和返回结果，业务逻辑下沉到 Service。
     */
    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> menus(@RequestHeader("Authorization") String authorization) {
        List<MenuResponse> menus = sysMenuService.getMenusByToken(authorization);
        return ApiResponse.success(menus);
    }
}