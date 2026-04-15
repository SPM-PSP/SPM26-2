package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;

@Data
public class JudgeResponse {
    private int code;
    private String status;
    private String message;
    private String compileLog;    // CE时返回
    private String runtimeLog;    // RE时返回
    private String diffLog;       // WA时返回
    private String userOutput;    // 原始输出
    private String formattedAnswer; // 格式化后标准答案
    private String formattedOutput; // 格式化后用户输出
}