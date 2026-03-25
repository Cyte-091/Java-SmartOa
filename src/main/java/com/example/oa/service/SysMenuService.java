package com.example.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oa.model.SysMenu;
import com.example.oa.rbac.dto.MenuResponse;

import java.util.List;
import java.util.Map;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据登录 token 返回当前用户可见菜单。
     */
    List<MenuResponse> getMenusByToken(String authorization);
}