package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI评价表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 提交ID
     */
    private Long submissionId;

    /**
     * 时间复杂度
     */
    private String timeComplexity;

    /**
     * 空间复杂度
     */
    private String spaceComplexity;

    /**
     * 代码风格评价
     */
    private String codeStyle;

    /**
     * 可读性评价
     */
    private String readability;

    /**
     * 优化建议
     */
    private String optimization;

    /**
     * 调用AI模型
     */
    private String aiModel;

    /**
     * 调用成本
     */
    private BigDecimal cost;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}