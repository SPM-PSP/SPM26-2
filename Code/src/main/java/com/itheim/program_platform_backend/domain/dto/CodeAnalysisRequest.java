package com.itheim.program_platform_backend.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeAnalysisRequest {
    /** 待分析的代码内容 */
    @NotBlank(message = "代码内容不能为空")
    private String code;

    /** 代码语言（Java/Python/C++） */
    @NotBlank(message = "代码语言不能为空")
    private String language;
}