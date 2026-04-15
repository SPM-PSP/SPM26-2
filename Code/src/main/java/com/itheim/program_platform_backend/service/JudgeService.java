package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;

public interface JudgeService {
    JudgeResponse judge(JudgeRequest request);
    boolean checkDockerStatus();
}