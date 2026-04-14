package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户题目状态表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProblem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
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
     * 题目状态：0-未通过 1-已通过
     */
    private Integer status;

    /**
     * 最后提交时间
     */
    private LocalDateTime lastSubmitTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}