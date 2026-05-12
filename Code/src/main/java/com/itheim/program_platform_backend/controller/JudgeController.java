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
@Api(tags = "判题接口")
public class JudgeController {

    private final JudgeService judgeService;

    @ApiOperation("代码判题接口（支持C++、Java、Python）")
    @PostMapping("/submit")
    public ResponseEntity<JudgeResponse> judge(@Valid @RequestBody JudgeRequest request) {
        return ResponseEntity.ok(judgeService.judge(request));
    }

    @ApiOperation("Docker健康检查")
    @PostMapping("/check-docker")
    public ResponseEntity<Boolean> checkDocker() {
        return ResponseEntity.ok(judgeService.checkDockerStatus());
    }
}
