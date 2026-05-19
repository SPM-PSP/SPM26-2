package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.domain.dto.SubmitToBackRequest;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.ProblemTestCase;
import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.domain.po.Submission;
import com.itheim.program_platform_backend.domain.vo.JudgeResponseVO;
import com.itheim.program_platform_backend.enums.JudgeResultEnum;
import com.itheim.program_platform_backend.mapper.AdminMapper;
import com.itheim.program_platform_backend.mapper.ProblemMapper;
import com.itheim.program_platform_backend.mapper.SubmissionMapper;
import com.itheim.program_platform_backend.service.JudgeService;
import com.itheim.program_platform_backend.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final SubmissionMapper submissionMapper;

    @Value("${file-storage.test-case-root:src/main/resources/testcases/}")
    private String testCaseRoot;

    @ApiOperation("提交代码并触发评测（需要登录）")
    @PostMapping("/submit_to_back")
    public Result<JudgeResponseVO> submitToBack(@Valid @RequestBody SubmitToBackRequest request) {
        Long userId = UserContext.getCurrentUserId();
        log.info("用户 {} 提交题目 {} 的代码，语言: {}", userId, request.getProblemId(), request.getLanguage());

        Problem problem = problemMapper.selectProblemById(request.getProblemId());
        if (problem == null) {
            log.warn("题目不存在: problemId={}", request.getProblemId());
            return new Result<>(400, "题目不存在", null);
        }

        List<ProblemTestCase> testCases = adminMapper.selectProblemTestCasesByProblemId(request.getProblemId());
        if (testCases == null || testCases.isEmpty()) {
            log.warn("题目没有测试用例: problemId={}", request.getProblemId());
            return new Result<>(400, "题目暂无测试用例", null);
        }

        ProblemTestCase firstTestCase = testCases.get(0);
        log.info("测试用例ID: {}, inputUrl: {}, outputUrl: {}",
                firstTestCase.getId(), firstTestCase.getInputUrl(), firstTestCase.getOutputUrl());

        String input = readTestCaseContent(firstTestCase.getInputUrl());
        String answer = readTestCaseContent(firstTestCase.getOutputUrl());

        if (input == null) {
            log.error("读取测试输入失败: caseId={}, url={}", firstTestCase.getId(), firstTestCase.getInputUrl());
            return new Result<>(500, "读取测试输入失败", null);
        }

        if (answer == null) {
            log.error("读取测试输出失败: caseId={}, url={}", firstTestCase.getId(), firstTestCase.getOutputUrl());
            return new Result<>(500, "读取测试输出失败", null);
        }

        log.info("成功读取测试用例，输入长度: {}, 答案长度: {}", input.length(), answer.length());

        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setCode(request.getCode());
        judgeRequest.setLanguage(request.getLanguage());
        judgeRequest.setInput(input);
        judgeRequest.setAnswer(answer);
        judgeRequest.setTimeLimit(problem.getTimeLimit());
        judgeRequest.setMemoryLimit(String.valueOf(problem.getMemoryLimit()));

        JudgeResponse judgeResponse = judgeService.judge(judgeRequest);

        log.info("判题完成: userId={}, problemId={}, status={}", userId, request.getProblemId(), judgeResponse.getStatus());

        // 将判题结果转换为提交记录状态码
        Integer statusCode = convertStatusToCode(judgeResponse.getStatus());

        // 计算通过的测试用例数（这里简化为：AC则全部通过，否则为0）
        Integer passCount = statusCode == 0 ? 1 : 0;
        Integer totalCount = 1; // 当前只使用第一个测试用例

        // 创建提交记录
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setProblemTitle(problem.getTitle());
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        submission.setStatus(statusCode);
        submission.setPassCount(passCount);
        submission.setTotalCount(totalCount);
        submission.setRunTime(0); // 可以从判题日志中解析
        submission.setMemory(0);  // 可以从判题日志中解析
        
        // 构建详细错误信息
        String detailedErrorMsg = buildDetailedErrorMessage(judgeResponse);
        submission.setErrorMsg(detailedErrorMsg);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreateTime(LocalDateTime.now());

        // 插入提交记录到数据库
        try {
            submissionMapper.insertSubmission(submission);
            log.info("提交记录插入成功: submissionId={}", submission.getId());
        } catch (Exception e) {
            log.error("插入提交记录失败", e);
        }

        // 构建返回给前端的响应
        JudgeResponseVO responseVO = new JudgeResponseVO();
        responseVO.setSubmissionId(submission.getId());
        responseVO.setRunTime(submission.getRunTime());
        responseVO.setMemory(submission.getMemory());
        responseVO.setErrorMsg(detailedErrorMsg);
        responseVO.setSubmitTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        responseVO.setResult(statusCode);
        responseVO.setPassCount(passCount);
        responseVO.setTotalCount(totalCount);

        return Result.success(responseVO);
    }

    /**
     * 构建详细的错误信息
     */
    private String buildDetailedErrorMessage(JudgeResponse judgeResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append(judgeResponse.getMessage());
        
        // 添加编译错误日志
        if (judgeResponse.getCompileLog() != null && !judgeResponse.getCompileLog().isEmpty()) {
            sb.append("\n\n编译错误详情：\n").append(judgeResponse.getCompileLog());
        }
        
        // 添加运行时错误日志
        if (judgeResponse.getRuntimeLog() != null && !judgeResponse.getRuntimeLog().isEmpty()) {
            sb.append("\n\n运行时错误详情：\n").append(judgeResponse.getRuntimeLog());
        }
        
        // 添加答案错误差异信息
        if (judgeResponse.getDiffLog() != null && !judgeResponse.getDiffLog().isEmpty()) {
            sb.append("\n\n答案差异：\n").append(judgeResponse.getDiffLog());
        }
        
        // 添加用户输出
        if (judgeResponse.getUserOutput() != null && !judgeResponse.getUserOutput().isEmpty()) {
            sb.append("\n\n你的输出：\n").append(judgeResponse.getUserOutput());
        }
        
        return sb.toString();
    }

    /**
     * 将判题状态字符串转换为前端使用的状态码
     * 0-通过 1-编译错误 2-运行错误 3-超时
     */
    private Integer convertStatusToCode(String status) {
        if (status == null) {
            return -1;
        }
        switch (status.toUpperCase()) {
            case "AC":
                return 0; // 通过
            case "CE":
                return 1; // 编译错误
            case "RE":
                return 2; // 运行错误
            case "TLE":
                return 3; // 超时
            case "MLE":
                return 2; // 内存超限归类为运行错误
            case "WA":
                return 2; // 答案错误归类为运行错误
            default:
                return -1; // 系统错误
        }
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
