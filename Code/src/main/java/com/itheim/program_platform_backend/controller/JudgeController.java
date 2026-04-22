package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.service.JudgeService;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/judge")
@RequiredArgsConstructor
@Api(tags = "judge认证接口")
public class JudgeController {

    private final JudgeService judgeService;

    /**
     * C++代码评测接口
     */
    @ApiOperation("判题接口")
    @PostMapping("/cpp")
    public ResponseEntity<JudgeResponse> judge(@Valid @RequestBody JudgeRequest request) {
        return ResponseEntity.ok(judgeService.judge(request));
    }

    /**
     * Docker健康检查接口
     */
    @ApiOperation("docker启动确认")
    @PostMapping("/check-docker")
    public ResponseEntity<Boolean> checkDocker() {
        return ResponseEntity.ok(judgeService.checkDockerStatus());
    }
}