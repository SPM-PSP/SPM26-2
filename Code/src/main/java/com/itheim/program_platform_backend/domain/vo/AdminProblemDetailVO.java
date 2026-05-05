package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProblemDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long problemId;
    private String title;
    private String difficulty;
    private List<String> categoryNames;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String sampleInput;
    private String sampleOutput;
    private Integer timeLimit;
    private Integer memoryLimit;
    private BigDecimal acceptRate;
    private List<AdminTestCaseVO> testCases;
}

