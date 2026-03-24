package com.example.oa.auth.dto;


import lombok.Data;



@Data
public class LoginResponse {

    private Long id;

    private String username;

    private Integer status;

    private String token;

}
