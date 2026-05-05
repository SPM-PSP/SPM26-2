package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTestCaseFileVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String inputUrl;
    private long inputSize;
    private String inputType;
    private String outputUrl;
    private long outputSize;
    private String outputType;
}

