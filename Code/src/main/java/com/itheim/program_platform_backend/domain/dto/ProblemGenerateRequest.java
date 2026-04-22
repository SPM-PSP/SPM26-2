package com.itheim.program_platform_backend.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemGenerateRequest {
    /** 算法板块（数组/链表/动态规划等） */
    @NotBlank(message = "算法板块不能为空")
    private String plate;

    /** 题目难度（简单/中等/困难） */
    @NotBlank(message = "题目难度不能为空")
    private String difficulty;

    /** 目标语言（Java/Python/C++） */
    @NotBlank(message = "目标语言不能为空")
    private String targetLanguage;
}