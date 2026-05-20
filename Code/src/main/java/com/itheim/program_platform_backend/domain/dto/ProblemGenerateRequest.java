package com.itheim.program_platform_backend.domain.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProblemGenerateRequest {
    /** 算法板块列表（数组/链表/动态规划等），支持多选 */
    @NotEmpty(message = "算法板块不能为空")
    private List<String> plates;

    /** 题目难度（简单/中等/困难） */
    @NotEmpty(message = "题目难度不能为空")
    private String difficulty;

    /** 目标语言（Java/Python/C++） */
    @NotEmpty(message = "目标语言不能为空")
    private String targetLanguage;
}