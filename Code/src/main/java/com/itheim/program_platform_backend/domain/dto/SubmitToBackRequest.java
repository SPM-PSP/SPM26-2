package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SubmitToBackRequest {
    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    @NotBlank(message = "编程语言不能为空")
    private String language;

    @NotBlank(message = "代码不能为空")
    private String code;
}
