package com.itheim.program_platform_backend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTestCaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String inputUrl;
    private String outputUrl;
    private LocalDateTime createTime;
}

