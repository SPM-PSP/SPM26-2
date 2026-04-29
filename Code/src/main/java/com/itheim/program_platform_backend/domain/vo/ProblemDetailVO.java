package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 题目详情VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailVO implements Serializable {
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
     * 题目描述
     */
    private String description;

    /**
     * 输入格式描述
     */
    private String inputFormat;

    /**
     * 输出格式描述
     */
    private String outputFormat;

    /**
     * 样例输入
     */
    private String sampleInput;

    /**
     * 样例输出
     */
    private String sampleOutput;

    /**
     * 运行时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制(KB)
     */
    private Integer memoryLimit;

    /**
     * 题目整体通过率
     */
    private BigDecimal acceptRate;

    /**
     * 用户当前题目完成状态（仅登录用户返回）
     */
    private Integer userStatus;
}
