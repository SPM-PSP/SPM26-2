package com.itheim.program_platform_backend.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProblemGenerateResponse {
    private String problemName;

    private String problemDesc;

    private List<String> sampleInput;

    private List<String> sampleOutput;

    private String inputFormat;

    private String outputFormat;
}
