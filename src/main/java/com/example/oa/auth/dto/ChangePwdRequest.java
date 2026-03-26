package com.example.oa.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePwdRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
