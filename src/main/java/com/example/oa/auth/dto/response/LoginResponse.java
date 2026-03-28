package com.example.oa.auth.dto.response;


import com.example.oa.model.SysUser;
import lombok.Data;



@Data
public class LoginResponse {

    private Long id;

    private String username;

    private Integer status;

    private String displayName;

    private String token;
}
