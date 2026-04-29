package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.vo.CategoryVO;
import com.itheim.program_platform_backend.domain.vo.PageResult;
import com.itheim.program_platform_backend.domain.vo.ProblemDetailVO;
import com.itheim.program_platform_backend.domain.vo.ProblemListItemVO;
import com.itheim.program_platform_backend.domain.vo.ProblemSolutionVO;

import java.util.List;

/**
 * 题库管理Service
 */
public interface ProblemService {

    /**
     * 获取题目分类列表
     */
    List<CategoryVO> getCategoryList();

    /**
     * 获取题库题目列表
     */
    PageResult<ProblemListItemVO> getProblemList(int page, int size, List<String> problemCategory,
                                                  String difficulty, String keyword, Integer status, Long userId);

    /**
     * 获取题目详情
     */
    ProblemDetailVO getProblemDetail(Long problemId, Long userId);

    /**
     * 获取题目题解
     */
    ProblemSolutionVO getProblemSolution(Long problemId);
}
