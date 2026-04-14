package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 提交ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 题目标题
     */
    private String problemTitle;

    /**
     * 语言：C++/Java
     */
    private String language;

    /**
     * 提交代码
     */
    private String code;

    /**
     * 评测状态
     */
    private Integer status;

    /**
     * 通过用例数
     */
    private Integer passCount;

    /**
     * 总用例数
     */
    private Integer totalCount;

    /**
     * 运行时间(ms)
     */
    private Integer runTime;

    /**
     * 内存占用(KB)
     */
    private Integer memory;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}