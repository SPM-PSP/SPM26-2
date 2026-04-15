package com.itheim.program_platform_backend.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JudgeRequest {
    @NotBlank(message = "C++代码不能为空")
    private String code;

    @NotBlank(message = "测试输入不能为空")
    private String input;

    @NotBlank(message = "标准答案不能为空")
    private String answer;

    // 可选，覆盖默认限制
    private Integer timeLimit;
    private String memoryLimit;
}