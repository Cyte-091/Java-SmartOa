package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜单实体（sys_menu）。
 */
@TableName(value = "sys_menu")
@Data
public class SysMenu implements Serializable {

    /**
     * 菜单主键 ID（自增）。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父级菜单 ID：0 表示顶级菜单。
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 菜单名称（前端展示用）。
     */
    @TableField(value = "menu_name")
    private String menuName;

    /**
     * 菜单路由路径，如 /approval。
     */
    @TableField(value = "path")
    private String path;

    /**
     * 前端组件名，如 HomeView。
     */
    @TableField(value = "component")
    private String component;

    /**
     * 权限点编码，如 approval:read，可为空。
     */
    @TableField(value = "permission_code")
    private String permissionCode;

    /**
     * 菜单排序号，值越小越靠前。
     */
    @TableField(value = "sort_no")
    private Integer sortNo;

    /**
     * 是否显示：1=显示，0=隐藏。
     */
    @TableField(value = "visible")
    private Integer visible;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}