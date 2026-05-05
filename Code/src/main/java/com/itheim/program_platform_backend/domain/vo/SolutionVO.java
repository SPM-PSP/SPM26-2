package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题解项VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题解ID
     */
    private Long solutionId;

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
     * 题解创建人用户名
     */
    private String createUserName;

    /**
     * 题解创建时间
     */
    private LocalDateTime createTime;
}
