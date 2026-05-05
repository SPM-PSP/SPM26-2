package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 题目题解列表VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSolutionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 题解列表（空则代表还没有题解）
     */
    private List<SolutionVO> solution;
}
