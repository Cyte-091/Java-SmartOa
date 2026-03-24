package com.example.oa.common.exception;

import com.example.oa.constants.user.ErrorEnum;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException{
    private final int code;

    public UserException(String message) {
        super(message);
        this.code = ErrorEnum.LOGIN_ERROR.getCode();
    }

    public UserException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();
    }

    public UserException(ErrorEnum errorEnum, String message) {
        super(message);
        this.code = errorEnum.getCode();
    }

}
