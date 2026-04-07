package com.itheim.program_platform_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonResultCode implements ResultCode {
    SUCCESS(200, "操作成功"),
    BUSINESS_ERROR(400, "业务处理失败"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "接口不存在");

    private final int code;
    private final String message;
}
