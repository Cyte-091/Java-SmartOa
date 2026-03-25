package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色-菜单关联实体（sys_role_menu）。
 * 一条记录表示“某个角色拥有某个菜单权限”。
 */
@TableName(value = "sys_role_menu")
@Data
public class SysRoleMenu implements Serializable {

    /**
     * 关联记录主键 ID（自增）。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色 ID（关联 sys_role.id）。
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 菜单 ID（关联 sys_menu.id）。
     */
    @TableField(value = "menu_id")
    private Long menuId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}