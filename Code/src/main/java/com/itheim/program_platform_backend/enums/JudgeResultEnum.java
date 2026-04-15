package com.itheim.program_platform_backend.enums;

import lombok.Getter;

@Getter
public enum JudgeResultEnum {
    AC(0, "AC", "答案正确"),
    CE(2, "CE", "编译错误"),
    TLE(137, "TLE", "时间/内存超限"),
    RE(3, "RE", "运行时错误"),
    WA(4, "WA", "答案错误"),
    SYSTEM_ERROR(-1, "SYSTEM_ERROR", "系统内部错误");

    private final int exitCode;
    private final String status;
    private final String message;

    JudgeResultEnum(int exitCode, String status, String message) {
        this.exitCode = exitCode;
        this.status = status;
        this.message = message;
    }

    public static JudgeResultEnum getByExitCode(int exitCode) {
        for (JudgeResultEnum result : values()) {
            if (result.exitCode == exitCode) return result;
        }
        return SYSTEM_ERROR;
    }
}