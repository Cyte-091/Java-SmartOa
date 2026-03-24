package com.example.oa.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
public class SysUser implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "username")
    private String username;

    /**
     * 
     */
    @TableField(value = "password")
    private String password;

    /**
     * 
     */
    @TableField(value = "display_name")
    private String displayName;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}