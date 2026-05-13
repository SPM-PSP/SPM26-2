package com.itheim.program_platform_backend.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminTestCaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 测试用例主键，供删除/更新接口使用 */
    private Long caseId;
    private String inputUrl;
    private String outputUrl;
    private LocalDateTime createTime;
}

