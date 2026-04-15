package com.itheim.program_platform_backend.exception;

import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.enums.CommonResultCode;
import com.itheim.program_platform_backend.enums.JudgeResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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




    @ExceptionHandler(JudgeException.class)
    public ResponseEntity<JudgeResponse> handleJudgeException(JudgeException e) {
        log.error("评测业务异常", e);
        JudgeResponse resp = new JudgeResponse();
        resp.setCode(JudgeResultEnum.SYSTEM_ERROR.getExitCode());
        resp.setStatus(JudgeResultEnum.SYSTEM_ERROR.getStatus());
        resp.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JudgeResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String msg = error != null ? error.getDefaultMessage() : "参数错误";
        JudgeResponse resp = new JudgeResponse();
        resp.setCode(JudgeResultEnum.SYSTEM_ERROR.getExitCode());
        resp.setStatus(JudgeResultEnum.SYSTEM_ERROR.getStatus());
        resp.setMessage(msg);
        return ResponseEntity.badRequest().body(resp);
    }

}