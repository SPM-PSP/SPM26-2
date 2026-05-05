package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 题目列表项VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目难度
     */
    private String difficulty;

    /**
     * 所属分类名称列表
     */
    private List<String> categoryNames;

    /**
     * 题目整体通过率（百分比）
     */
    private BigDecimal acceptRate;

    /**
     * 用户完成状态（仅登录用户返回）
     */
    private Integer status;
}
