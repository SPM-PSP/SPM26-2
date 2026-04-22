package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;

@Data
public class ProblemGenerateResponse {
    /** 题目名称 */
    private String problemName;

    /** 题目描述 */
    private String problemDesc;

    /** 样例输入 */
    private String sampleInput;

    /** 样例输出 */
    private String sampleOutput;

    /** 输入格式说明 */
    private String inputFormat;

    /** 输出格式说明 */
    private String outputFormat;
}