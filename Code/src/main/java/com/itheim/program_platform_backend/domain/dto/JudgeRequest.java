package com.itheim.program_platform_backend.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JudgeRequest {
    @NotBlank(message = "代码不能为空")
    private String code;

    @NotBlank(message = "测试输入不能为空")
    private String input;

    @NotBlank(message = "标准答案不能为空")
    private String answer;

    @NotBlank(message = "编程语言不能为空")
    private String language;

    private Integer timeLimit;
    private String memoryLimit;
}
