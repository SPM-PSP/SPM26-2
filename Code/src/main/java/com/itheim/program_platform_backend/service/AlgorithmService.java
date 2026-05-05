package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.CodeAnalysisRequest;
import com.itheim.program_platform_backend.domain.dto.CodeAnalysisResponse;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateRequest;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateResponse;

public interface AlgorithmService {
    /**
     * 代码分析（分类+复杂度+风格）
     * @param request 代码分析请求
     * @return 分析结果
     */
    CodeAnalysisResponse analyzeCode(CodeAnalysisRequest request);

    /**
     * 生成指定板块的算法题
     * @param request 题目生成请求
     * @return 生成的题目信息
     */
    ProblemGenerateResponse generateProblem(ProblemGenerateRequest request);
}