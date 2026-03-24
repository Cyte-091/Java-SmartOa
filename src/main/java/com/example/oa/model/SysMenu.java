package com.example.oa.model;


import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName sys_menu
 */
@TableName(value ="sys_menu")
@Data
public class SysMenu implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 
     */
    @TableField(value = "menu_name")
    private String menuName;

    /**
     * 
     */
    @TableField(value = "path")
    private String path;

    /**
     * 
     */
    @TableField(value = "component")
    private String component;

    /**
     * 
     */
    @TableField(value = "permission_code")
    private String permissionCode;

    /**
     * 
     */
    @TableField(value = "sort_no")
    private Integer sortNo;

    /**
     * 
     */
    @TableField(value = "visible")
    private Integer visible;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}