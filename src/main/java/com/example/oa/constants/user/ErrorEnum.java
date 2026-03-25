package com.example.oa.constants.user;

import lombok.Getter;

@Getter
public enum ErrorEnum {

    LOGIN_ERROR(1001, "登录失败"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已禁用"),
    TOKEN_INVALID(1005, "token 不合法"),
    SUCCESS(200, "登录成功");

    private final int code;
    private final String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}