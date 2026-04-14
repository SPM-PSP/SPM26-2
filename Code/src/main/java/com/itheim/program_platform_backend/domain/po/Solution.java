package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题解表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solution implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题解ID
     */
    private Long id;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解内容
     */
    private String content;

    /**
     * 题解语言
     */
    private String language;

    /**
     * 题解代码
     */
    private String code;

    /**
     * 创建人
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}