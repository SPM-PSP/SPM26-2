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
public class AdminProblemListItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long problemId;
    private String title;
    private String difficulty;
    private List<String> categoryNames;
    private BigDecimal acceptRate;
}

