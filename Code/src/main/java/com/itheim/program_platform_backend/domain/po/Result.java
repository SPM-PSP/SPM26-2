package com.itheim.program_platform_backend.domain.po;

import com.itheim.program_platform_backend.enums.CommonResultCode;
import com.itheim.program_platform_backend.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return of(CommonResultCode.SUCCESS, data);
    }

    /**
     * 成功响应（无数据）
     */
    public static Result<Void> success() {
        return of(CommonResultCode.SUCCESS, null);
    }

    public static Result<Void> success(String message) {
        Result<Void> result = new Result<>();
        result.setCode(CommonResultCode.SUCCESS.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 成功响应（自定义消息，带数据）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(CommonResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     */
    public static Result<Void> fail(ResultCode resultCode) {
        return of(resultCode, null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static Result<Void> fail(int code, String message) {
        Result<Void> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 根据 ResultCode 和数据构建结果
     */
    public static <T> Result<T> of(ResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(data);
        return result;
    }

}
