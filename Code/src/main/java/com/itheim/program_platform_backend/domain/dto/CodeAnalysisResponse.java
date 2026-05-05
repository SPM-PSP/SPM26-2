package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;

@Data
public class CodeAnalysisResponse {
    /** 算法板块分类（数组/链表/动态规划/贪心等） */
    private String plateCategory;

    /** 代码复杂度分析（时间+空间） */
    private String complexityAnalysis;

    /** 代码风格分析（命名/注释/格式/可读性） */
    private String codeStyleAnalysis;
}