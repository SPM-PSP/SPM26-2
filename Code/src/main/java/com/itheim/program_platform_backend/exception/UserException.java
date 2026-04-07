package com.itheim.program_platform_backend.exception;

import com.itheim.program_platform_backend.enums.ResultCode;

/**
 * 用户相关异常
 */
public class UserException extends BusinessException {
    
    public UserException(ResultCode resultCode) {
        super(resultCode);
    }
    
    public UserException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
    
    public UserException(int code, String message) {
        super(code, message);
    }
}
