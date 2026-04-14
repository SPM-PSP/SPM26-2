package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 难度：简单/中等/困难
     */
    private String difficulty;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 输入格式
     */
    private String inputFormat;

    /**
     * 输出格式
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
     * 题目通过率(%)
     */
    private BigDecimal passRate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}