package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户-角色关联实体（sys_user_role）。
 * 一条记录表示“某个用户拥有某个角色”。
 */
@TableName(value = "sys_user_role")
@Data
public class SysUserRole implements Serializable {

    /**
     * 关联记录主键 ID（自增）。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID（关联 sys_user.id）。
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 角色 ID（关联 sys_role.id）。
     */
    @TableField(value = "role_id")
    private Long roleId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}