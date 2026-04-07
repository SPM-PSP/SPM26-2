package com.itheim.program_platform_backend.exception;

import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.enums.CommonResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理所有未被捕获的 Exception
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 记录错误日志
        log.error("系统异常: ", e);
        return Result.fail(CommonResultCode.SYSTEM_ERROR);
    }
}