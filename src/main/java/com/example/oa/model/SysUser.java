package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体（sys_user）。
 */
@TableName(value = "sys_user")
@Data
public class SysUser implements Serializable {

    /**
     * 用户主键 ID（自增）。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名（唯一）。
     */
    @TableField(value = "username")
    private String username;

    /**
     * 登录密码（当前项目阶段为明文，后续建议改为 BCrypt 哈希）。
     */
    @TableField(value = "password")
    private String password;

    /**
     * 用户显示名称（页面展示名）。
     */
    @TableField(value = "display_name")
    private String displayName;

    /**
     * 用户状态：1=启用，0=禁用。
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间。
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}