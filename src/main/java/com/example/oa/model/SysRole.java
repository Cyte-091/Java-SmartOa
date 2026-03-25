package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体（sys_role）。
 */
@TableName(value = "sys_role")
@Data
public class SysRole implements Serializable {

    /**
     * 角色主键 ID（自增）。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（唯一），如 ADMIN、EMPLOYEE。
     */
    @TableField(value = "role_code")
    private String roleCode;

    /**
     * 角色名称（展示用），如 Administrator。
     */
    @TableField(value = "role_name")
    private String roleName;

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