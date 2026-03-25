package com.example.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oa.auth.dto.LoginResponse;
import com.example.oa.mapper.SysMenuMapper;
import com.example.oa.model.SysMenu;
import com.example.oa.model.SysRole;
import com.example.oa.model.SysUserRole;
import com.example.oa.rbac.dto.MenuResponse;
import com.example.oa.service.SysMenuService;
import com.example.oa.service.SysRoleService;
import com.example.oa.service.SysUserRoleService;
import com.example.oa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 根据 token 返回当前用户可见菜单。
     * 规则：
     * 1) 所有登录用户都返回：首页、审批中心
     * 2) 角色中包含 ADMIN 时，额外返回：系统管理
     */
    @Override
    public List<MenuResponse> getMenusByToken(String authorization) {
        List<MenuResponse> result = new ArrayList<>();

        // 固定菜单1：首页（所有登录用户都有）
        MenuResponse dashboard = new MenuResponse();
        dashboard.setName("首页");
        dashboard.setPath("/");
        dashboard.setComponent("HomeView");
        result.add(dashboard);

        // 固定菜单2：审批中心（所有登录用户都有）
        MenuResponse approval = new MenuResponse();
        approval.setName("审批中心");
        approval.setPath("/approval");
        approval.setComponent("ApprovalView");
        result.add(approval);

        // 1) 解析当前登录用户
        LoginResponse currentUser = sysUserService.getCurrentUser(authorization);

        // 2) 根据 user_id 查询用户-角色关系
        QueryWrapper<SysUserRole> userRoleQw = new QueryWrapper<>();
        userRoleQw.eq("user_id", currentUser.getId());
        List<SysUserRole> userRoleList = sysUserRoleService.list(userRoleQw);

        List<Long> roleIds = new ArrayList<>();
        for (SysUserRole userRole : userRoleList) {
            if (userRole.getRoleId() != null) {
                roleIds.add(userRole.getRoleId());
            }
        }

        // 3) 根据 role_id 查询角色信息，判断是否存在 ADMIN
        boolean isAdmin = false;
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            for (SysRole role : roles) {
                if ("ADMIN".equalsIgnoreCase(role.getRoleCode())) {
                    isAdmin = true;
                    break;
                }
            }
        }

        // 4) 管理员额外拥有“系统管理”菜单
        if (isAdmin) {
            MenuResponse system = new MenuResponse();
            system.setName("系统管理");
            system.setPath("/system");
            system.setComponent("SystemView");
            result.add(system);
        }

        return result;
    }
}