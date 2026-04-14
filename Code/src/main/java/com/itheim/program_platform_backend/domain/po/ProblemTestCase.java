package com.itheim.program_platform_backend.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目测试样例表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 测试样例ID
     */
    private Long id;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 输入文件URL
     */
    private String inputUrl;

    /**
     * 输出文件URL
     */
    private String outputUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}