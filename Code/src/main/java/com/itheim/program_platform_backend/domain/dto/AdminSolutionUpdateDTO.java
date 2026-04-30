package com.itheim.program_platform_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminSolutionUpdateDTO implements Serializable {
    private Long solutionId;
    private String title;
    private String content;
    private String language;
    private String code;
}

