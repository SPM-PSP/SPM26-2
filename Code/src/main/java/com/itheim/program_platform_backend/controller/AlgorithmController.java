package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.CodeAnalysisRequest;
import com.itheim.program_platform_backend.domain.dto.CodeAnalysisResponse;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateRequest;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateResponse;
import com.itheim.program_platform_backend.service.AlgorithmService;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "分析")
@RestController
@RequestMapping("/api/algorithm")
@RequiredArgsConstructor
public class AlgorithmController {
    private final AlgorithmService algorithmService;

    /**
     * 代码分析接口
     */
    @ApiOperation("代码分析")
    @PostMapping("/analyze-code")
    public ResponseEntity<CodeAnalysisResponse> analyzeCode(@Valid @RequestBody CodeAnalysisRequest request) {
        CodeAnalysisResponse response = algorithmService.analyzeCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 算法题生成接口
     */
    @ApiOperation("算法题生成")
    @PostMapping("/generate-problem")
    public ResponseEntity<ProblemGenerateResponse> generateProblem(@Valid @RequestBody ProblemGenerateRequest request) {
        ProblemGenerateResponse response = algorithmService.generateProblem(request);
        return ResponseEntity.ok(response);
    }
}