package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.domain.dto.SubmitToBackRequest;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.ProblemTestCase;
import com.itheim.program_platform_backend.mapper.AdminMapper;
import com.itheim.program_platform_backend.mapper.ProblemMapper;
import com.itheim.program_platform_backend.service.JudgeService;
import com.itheim.program_platform_backend.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/judge")
@RequiredArgsConstructor
@Api(tags = "判题接口V1")
public class JudgeV1Controller {

    private final JudgeService judgeService;
    private final ProblemMapper problemMapper;
    private final AdminMapper adminMapper;

    @Value("${file-storage.test-case-root:src/main/resources/testcases/}")
    private String testCaseRoot;

    @ApiOperation("提交代码并触发评测（需要登录）")
    @PostMapping("/submit_to_back")
    public ResponseEntity<JudgeResponse> submitToBack(@Valid @RequestBody SubmitToBackRequest request) {
        Long userId = UserContext.getCurrentUserId();
        log.info("用户 {} 提交题目 {} 的代码，语言: {}", userId, request.getProblemId(), request.getLanguage());

        Problem problem = problemMapper.selectProblemById(request.getProblemId());
        if (problem == null) {
            log.warn("题目不存在: problemId={}", request.getProblemId());
            JudgeResponse errorResponse = new JudgeResponse();
            errorResponse.setCode(-1);
            errorResponse.setStatus("SYSTEM_ERROR");
            errorResponse.setMessage("题目不存在");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        List<ProblemTestCase> testCases = adminMapper.selectProblemTestCasesByProblemId(request.getProblemId());
        if (testCases == null || testCases.isEmpty()) {
            log.warn("题目没有测试用例: problemId={}", request.getProblemId());
            JudgeResponse errorResponse = new JudgeResponse();
            errorResponse.setCode(-1);
            errorResponse.setStatus("SYSTEM_ERROR");
            errorResponse.setMessage("题目暂无测试用例");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ProblemTestCase firstTestCase = testCases.get(0);
        log.info("测试用例ID: {}, inputUrl: {}, outputUrl: {}",
                firstTestCase.getId(), firstTestCase.getInputUrl(), firstTestCase.getOutputUrl());

        String input = readTestCaseContent(firstTestCase.getInputUrl());
        String answer = readTestCaseContent(firstTestCase.getOutputUrl());

        if (input == null) {
            log.error("读取测试输入失败: caseId={}, url={}", firstTestCase.getId(), firstTestCase.getInputUrl());
            JudgeResponse errorResponse = new JudgeResponse();
            errorResponse.setCode(-1);
            errorResponse.setStatus("SYSTEM_ERROR");
            errorResponse.setMessage("读取测试输入失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        if (answer == null) {
            log.error("读取测试输出失败: caseId={}, url={}", firstTestCase.getId(), firstTestCase.getOutputUrl());
            JudgeResponse errorResponse = new JudgeResponse();
            errorResponse.setCode(-1);
            errorResponse.setStatus("SYSTEM_ERROR");
            errorResponse.setMessage("读取测试输出失败");
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        log.info("成功读取测试用例，输入长度: {}, 答案长度: {}", input.length(), answer.length());

        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setCode(request.getCode());
        judgeRequest.setLanguage(request.getLanguage());
        judgeRequest.setInput(input);
        judgeRequest.setAnswer(answer);
        judgeRequest.setTimeLimit(problem.getTimeLimit());
        judgeRequest.setMemoryLimit(String.valueOf(problem.getMemoryLimit()));

        JudgeResponse response = judgeService.judge(judgeRequest);

        log.info("判题完成: userId={}, problemId={}, status={}", userId, request.getProblemId(), response.getStatus());

        return ResponseEntity.ok(response);
    }

    private String readTestCaseContent(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("测试用例URL为空");
            return null;
        }

        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                log.debug("从HTTP URL读取: {}", url);
                return readFromUrl(url);
            } else {
                log.debug("从本地文件读取: {}", url);
                return readFromFile(url);
            }
        } catch (Exception e) {
            log.error("读取测试用例内容失败: url={}", url, e);
            return null;
        }
    }

    private String readFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        } finally {
            conn.disconnect();
        }
    }

    private String readFromFile(String relativePath) throws Exception {
        Path rootPath = Paths.get(testCaseRoot).toAbsolutePath().normalize();
        Path filePath = rootPath.resolve(relativePath).normalize();

        log.debug("解析文件路径 - 根路径: {}, 相对路径: {}, 完整路径: {}",
                rootPath, relativePath, filePath);

        if (!filePath.startsWith(rootPath)) {
            log.error("文件路径不安全: {}", filePath);
            throw new IllegalArgumentException("文件路径不安全");
        }

        if (!Files.exists(filePath)) {
            log.error("测试用例文件不存在: {}", filePath);
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        byte[] bytes = Files.readAllBytes(filePath);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
