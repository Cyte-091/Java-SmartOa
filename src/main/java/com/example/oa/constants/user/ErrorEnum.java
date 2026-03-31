package com.example.oa.constants.user;

import lombok.Getter;

@Getter
public enum ErrorEnum {

    LOGIN_ERROR(1001, "登录失败"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已禁用"),
    TOKEN_INVALID(1005, "token无效"),
    PASSWORD_SHORT(1006, "密码至少要六位"),
    NOT_ADMIN(1007, "非管理员，权限不足"),
    PARAM_ERROR(1008, "用户状态非法"),
    SUCCESS(200, "登录成功");

    private final int code;
    private final String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}