package com.itheim.program_platform_backend.exception;

import com.itheim.program_platform_backend.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑中抛出特定的业务错误
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final String message;
    private final ResultCode resultCode;

    /**
     * 使用 ResultCode 枚举构造异常
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 使用 ResultCode 枚举构造异常，并自定义消息
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = customMessage;
    }

    /**
     * 直接使用 code 和 message 构造异常
     */
    public BusinessException(int code, String message) {
        super(message);
        this.resultCode = null;
        this.code = code;
        this.message = message;
    }

    /**
     * 使用 ResultCode 枚举构造异常，并携带原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}
