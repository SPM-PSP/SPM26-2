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
import com.itheim.program_platform_backend.service.JudgeResultCacheService;
import com.itheim.program_platform_backend.service.JudgeService;
import com.itheim.program_platform_backend.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private final JudgeResultCacheService resultCacheService;

    @Value("${file-storage.test-case-root:src/main/resources/testcases/}")
    private String testCaseRoot;

    @ApiOperation("提交代码并触发评测（需要登录）- 异步模式")
    @PostMapping("/submit_to_back")
    public Result<Map<String, Object>> submitToBack(@Valid @RequestBody SubmitToBackRequest request) {
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

        log.info("题目 {} 共有 {} 个测试用例", request.getProblemId(), testCases.size());

        // 先创建提交记录（状态为"判题中"）
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setProblemTitle(problem.getTitle());
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        submission.setStatus(-1); // -1 表示判题中
        submission.setPassCount(0);
        submission.setTotalCount(testCases.size());
        submission.setRunTime(0);
        submission.setMemory(0);
        submission.setErrorMsg("判题中...");
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreateTime(LocalDateTime.now());

        try {
            submissionMapper.insertSubmission(submission);
            log.info("提交记录创建成功: submissionId={}", submission.getId());
        } catch (Exception e) {
            log.error("创建提交记录失败", e);
            return new Result<>(500, "提交失败", null);
        }

        // 异步执行判题任务
        asyncJudgeTask(submission.getId(), request, problem, testCases);

        // 立即返回 submissionId
        Map<String, Object> response = new HashMap<>();
        response.put("submissionId", submission.getId());
        response.put("message", "提交成功，正在判题");
        response.put("status", "PENDING");

        return Result.success(response);
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
        
        // 注意：不再添加"你的输出"，由前端根据需要显示
        
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

    /**
     * 更新题目通过率
     * 通过率 = (通过的用户数 / 总提交用户数) * 100%
     */
    private void updateProblemPassRate(Long problemId) {
        try {
            // 查询该题目的总提交用户数（去重）
            Integer totalUsers = submissionMapper.countDistinctUsersByProblemId(problemId);
            if (totalUsers == null || totalUsers == 0) {
                log.debug("题目 {} 暂无提交记录", problemId);
                return;
            }
            
            // 查询该题目通过的用户数（去重，status=0表示通过）
            Integer acceptedUsers = submissionMapper.countDistinctAcceptedUsersByProblemId(problemId);
            if (acceptedUsers == null) {
                acceptedUsers = 0;
            }
            
            // 计算通过率（百分比）
            java.math.BigDecimal passRate = java.math.BigDecimal.ZERO;
            if (totalUsers > 0) {
                passRate = java.math.BigDecimal.valueOf(acceptedUsers)
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .divide(java.math.BigDecimal.valueOf(totalUsers), 2, java.math.RoundingMode.HALF_UP);
            }
            
            // 更新题目通过率
            problemMapper.updateProblemPassRate(problemId, passRate);
            log.info("题目 {} 通过率已更新: {}% (通过:{}/总:{})", problemId, passRate, acceptedUsers, totalUsers);
        } catch (Exception e) {
            log.error("更新题目 {} 通过率失败", problemId, e);
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

    /**
     * 异步判题任务
     * 在后台线程池中执行，不阻塞 HTTP 请求
     */
    @Async("judgeTaskExecutor")
    public void asyncJudgeTask(Long submissionId, SubmitToBackRequest request, 
                               Problem problem, List<ProblemTestCase> testCases) {
        log.info("开始异步判题: submissionId={}", submissionId);
        
        int passCount = 0;
        int totalCount = testCases.size();
        JudgeResponse judgeResponse = null;
        StringBuilder allErrors = new StringBuilder();
        boolean hasError = false;

        try {
            // 遍历所有测试用例进行判题
            for (int i = 0; i < testCases.size(); i++) {
                ProblemTestCase testCase = testCases.get(i);
                log.info("正在判题: submissionId={}, 测试用例 {}/{}", submissionId, i + 1, totalCount);

                String input = readTestCaseContent(testCase.getInputUrl());
                String answer = readTestCaseContent(testCase.getOutputUrl());

                if (input == null || answer == null) {
                    log.error("读取测试用例 {} 失败", i + 1);
                    continue;
                }

                JudgeRequest judgeRequest = new JudgeRequest();
                judgeRequest.setCode(request.getCode());
                judgeRequest.setLanguage(request.getLanguage());
                judgeRequest.setInput(input);
                judgeRequest.setAnswer(answer);
                judgeRequest.setTimeLimit(problem.getTimeLimit());
                judgeRequest.setMemoryLimit(String.valueOf(problem.getMemoryLimit()));

                judgeResponse = judgeService.judge(judgeRequest);

                // 判断是否通过
                if ("AC".equals(judgeResponse.getStatus())) {
                    passCount++;
                    log.info("测试用例 {}/{} 通过", i + 1, totalCount);
                } else {
                    hasError = true;
                    log.info("测试用例 {}/{} 失败: {}", i + 1, totalCount, judgeResponse.getStatus());
                    
                    allErrors.append(String.format("\n\n=== 测试用例 %d/%d ===\n", i + 1, totalCount));
                    allErrors.append("结果: ").append(judgeResponse.getStatus()).append("\n");
                    if (judgeResponse.getCompileLog() != null && !judgeResponse.getCompileLog().isEmpty()) {
                        allErrors.append("编译错误:\n").append(judgeResponse.getCompileLog()).append("\n");
                    }
                    if (judgeResponse.getRuntimeLog() != null && !judgeResponse.getRuntimeLog().isEmpty()) {
                        allErrors.append("运行时错误:\n").append(judgeResponse.getRuntimeLog()).append("\n");
                    }
                    if (judgeResponse.getDiffLog() != null && !judgeResponse.getDiffLog().isEmpty()) {
                        allErrors.append("答案差异:\n").append(judgeResponse.getDiffLog()).append("\n");
                    }
                    if (judgeResponse.getUserOutput() != null && !judgeResponse.getUserOutput().isEmpty()) {
                        allErrors.append("你的输出:\n").append(judgeResponse.getUserOutput()).append("\n");
                    }
                    
                    if ("CE".equals(judgeResponse.getStatus()) || "RE".equals(judgeResponse.getStatus())) {
                        log.info("编译错误或运行时错误，停止后续测试用例");
                        break;
                    }
                }
            }

            log.info("判题完成: submissionId={}, 通过数={}, 总数={}", submissionId, passCount, totalCount);

            Integer statusCode = hasError ? convertStatusToCode(judgeResponse.getStatus()) : 0;

            // 更新提交记录
            Submission submission = new Submission();
            submission.setId(submissionId);
            submission.setStatus(statusCode);
            submission.setPassCount(passCount);
            submission.setTotalCount(totalCount);
            
            if (judgeResponse != null) {
                submission.setRunTime(judgeResponse.getRunTime() != null ? judgeResponse.getRunTime() : 0);
                submission.setMemory(judgeResponse.getMemory() != null ? judgeResponse.getMemory() : 0);
            }
            
            String detailedErrorMsg = allErrors.toString();
            if (statusCode == 0 && detailedErrorMsg.isEmpty()) {
                detailedErrorMsg = "通过所有测试用例";
            }
            submission.setErrorMsg(detailedErrorMsg);

            submissionMapper.updateSubmission(submission);
            log.info("提交记录更新成功: submissionId={}, status={}", submissionId, statusCode);
            
            // 更新题目通过率
            updateProblemPassRate(request.getProblemId());

            // 构建响应对象并缓存
            JudgeResponseVO responseVO = new JudgeResponseVO();
            responseVO.setSubmissionId(submissionId);
            responseVO.setRunTime(submission.getRunTime());
            responseVO.setMemory(submission.getMemory());
            responseVO.setErrorMsg(detailedErrorMsg);
            responseVO.setSubmitTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            responseVO.setResult(statusCode);
            responseVO.setPassCount(passCount);
            responseVO.setTotalCount(totalCount);

            resultCacheService.saveResult(submissionId, responseVO);
            resultCacheService.updateStatus(submissionId, "COMPLETED");

        } catch (Exception e) {
            log.error("异步判题失败: submissionId={}", submissionId, e);
            
            // 更新为系统错误状态
            Submission submission = new Submission();
            submission.setId(submissionId);
            submission.setStatus(-2); // -2 表示系统错误
            submission.setErrorMsg("判题系统异常: " + e.getMessage());
            submissionMapper.updateSubmission(submission);
            
            resultCacheService.updateStatus(submissionId, "FAILED");
        }
    }

    @ApiOperation("查询判题结果")
    @GetMapping("/result/{submissionId}")
    public Result<JudgeResponseVO> getJudgeResult(@PathVariable Long submissionId) {
        // 先从缓存中查询
        JudgeResponseVO result = resultCacheService.getResult(submissionId);
        if (result != null) {
            return Result.success(result);
        }
        
        // 如果缓存中没有，从数据库查询
        Submission submission = submissionMapper.selectSubmissionById(submissionId);
        if (submission == null) {
            return new Result<>(404, "提交记录不存在", null);
        }
        
        if (submission.getStatus() == -1) {
            return new Result<>(200, "判题中", null);
        }
        
        // 构建响应
        JudgeResponseVO responseVO = new JudgeResponseVO();
        responseVO.setSubmissionId(submission.getId());
        responseVO.setRunTime(submission.getRunTime());
        responseVO.setMemory(submission.getMemory());
        responseVO.setErrorMsg(submission.getErrorMsg());
        responseVO.setSubmitTime(submission.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        responseVO.setResult(submission.getStatus());
        responseVO.setPassCount(submission.getPassCount());
        responseVO.setTotalCount(submission.getTotalCount());
        
        return Result.success(responseVO);
    }
}
