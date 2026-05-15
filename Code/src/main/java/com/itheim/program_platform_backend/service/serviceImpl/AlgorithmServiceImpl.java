package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.dto.CodeAnalysisRequest;
import com.itheim.program_platform_backend.domain.dto.CodeAnalysisResponse;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateRequest;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateResponse;
import com.itheim.program_platform_backend.domain.po.Problem;
import com.itheim.program_platform_backend.domain.po.ProblemTestCase;
import com.itheim.program_platform_backend.exception.BusinessException;
import com.itheim.program_platform_backend.mapper.AdminMapper;
import com.itheim.program_platform_backend.service.AlgorithmService;
import com.itheim.program_platform_backend.utils.VolcLlmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {
    private final VolcLlmUtil volcLlmUtil;
    private final AdminMapper adminMapper;

    @Value("${file-storage.test-case-root}")
    private String testCaseRoot;

    @Override
    public CodeAnalysisResponse analyzeCode(CodeAnalysisRequest request) {
        String prompt = String.format("""
                请完成以下对%s代码的分析任务，要求输出结构化结果：
                1. 算法板块分类：仅输出分类名称（如数组、链表、动态规划、贪心、回溯、图论等）；
                2. 代码复杂度分析：详细分析时间复杂度（最好/最坏/平均）和空间复杂度，说明分析依据；
                3. 代码风格分析：从变量命名、注释完整性、代码格式、逻辑可读性、异常处理等维度分析；
                代码内容：
                %s
                输出格式要求：
                【算法板块】：xxx
                【复杂度分析】：xxx
                【代码风格】：xxx
                """, request.getLanguage(), request.getCode());

        String llmResponse = volcLlmUtil.callLlm(prompt);
        log.info("代码分析响应：{}", llmResponse);

        CodeAnalysisResponse response = new CodeAnalysisResponse();

        response.setPlateCategory(extractField(llmResponse, "【算法板块】："));
        response.setComplexityAnalysis(extractField(llmResponse, "【复杂度分析】："));
        response.setCodeStyleAnalysis(extractField(llmResponse, "【代码风格】："));

        return response;
    }

    @Override
    public ProblemGenerateResponse generateProblem(ProblemGenerateRequest request) {
        String prompt = String.format("""
                请生成一道%s难度的%s算法题（目标语言：%s），要求输出结构化结果：
                1. 题目名称：简洁明了，符合算法题命名规范；
                2. 题目描述：详细说明题目要求、输入约束、数据范围；
                3. 样例输入：给出2个典型输入案例,要求第1个输入输出样例数量级在5以内，第二个在10-60之间（根据题目复杂度）；
                4. 样例输出：对应样例输入的输出结果；
                5. 输入格式：说明输入的格式规范；
                6. 输出格式：说明输出的格式规范；
                输出格式要求：
                【题目名称】：xxx
                【题目描述】：xxx
                【样例输入1】：xxx
                【样例输出1】：xxx
                【样例输入2】：xxx
                【样例输出2】：xxx
                【输入格式】：xxx
                【输出格式】：xxx
                """, request.getDifficulty(), request.getPlate(), request.getTargetLanguage());

        String llmResponse = volcLlmUtil.callLlm(prompt);
        log.info("题目生成响应：{}", llmResponse);

        ProblemGenerateResponse response = new ProblemGenerateResponse();

        response.setProblemName(extractField(llmResponse, "【题目名称】："));
        response.setProblemDesc(extractField(llmResponse, "【题目描述】："));

        List<String> sampleInputs = new ArrayList<>();
        sampleInputs.add(extractField(llmResponse, "【样例输入1】："));
        sampleInputs.add(extractField(llmResponse, "【样例输入2】："));
        response.setSampleInput(sampleInputs);

        List<String> sampleOutputs = new ArrayList<>();
        sampleOutputs.add(extractField(llmResponse, "【样例输出1】："));
        sampleOutputs.add(extractField(llmResponse, "【样例输出2】："));
        response.setSampleOutput(sampleOutputs);

        response.setInputFormat(extractField(llmResponse, "【输入格式】："));
        response.setOutputFormat(extractField(llmResponse, "【输出格式】："));

        // 保存题目和测试用例到数据库
        saveProblemAndTestCases(response, request);

        return response;
    }

    /**
     * 将AI生成的题目和测试用例保存到数据库
     */
    @Transactional
    public void saveProblemAndTestCases(ProblemGenerateResponse response, ProblemGenerateRequest request) {
        // 1. 创建题目实体
        Problem problem = new Problem();
        problem.setTitle(response.getProblemName());
        problem.setDifficulty(request.getDifficulty());
        problem.setDescription(response.getProblemDesc());
        problem.setInputFormat(response.getInputFormat());
        problem.setOutputFormat(response.getOutputFormat());
        // 将多个样例合并为一个字符串
        problem.setSampleInput(String.join("\n---\n", response.getSampleInput()));
        problem.setSampleOutput(String.join("\n---\n", response.getSampleOutput()));
        problem.setTimeLimit(5000); // 默认5秒
        problem.setMemoryLimit(256 * 1024); // 默认256MB
        problem.setPassRate(BigDecimal.ZERO);
        problem.setCreateTime(LocalDateTime.now());
        problem.setUpdateTime(LocalDateTime.now());

        // 2. 插入题目
        int rows = adminMapper.insertProblem(problem);
        if (rows == 0) {
            throw new BusinessException(500, "保存题目失败");
        }
        Long problemId = problem.getId();
        log.info("题目保存成功，ID: {}", problemId);

        // 3. 保存测试用例
        List<String> sampleInputs = response.getSampleInput();
        List<String> sampleOutputs = response.getSampleOutput();

        for (int i = 0; i < sampleInputs.size() && i < sampleOutputs.size(); i++) {
            saveTestCase(problemId, sampleInputs.get(i), sampleOutputs.get(i));
        }
        log.info("测试用例保存成功，题目ID: {}，数量: {}", problemId, sampleInputs.size());

        // 4. 添加默认分类（根据请求中的板块）
        String categoryName = request.getPlate();
        if (categoryName != null && !categoryName.isEmpty()) {
            addProblemCategory(problemId, categoryName);
        }
    }

    /**
     * 保存单个测试用例
     */
    private void saveTestCase(Long problemId, String inputContent, String outputContent) {
        // 生成文件名并保存
        String inputFileName = UUID.randomUUID() + ".in";
        String outputFileName = UUID.randomUUID() + ".out";
        String inputRelativePath = "problem-" + problemId + "/" + inputFileName;
        String outputRelativePath = "problem-" + problemId + "/" + outputFileName;

        try {
            // 确保目录存在
            Path inputPath = resolveSafePath(inputRelativePath);
            Path outputPath = resolveSafePath(outputRelativePath);
            Files.createDirectories(inputPath.getParent());

            // 写入文件内容
            Files.writeString(inputPath, inputContent, StandardCharsets.UTF_8);
            Files.writeString(outputPath, outputContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("保存测试用例文件失败", e);
            throw new BusinessException(500, "保存测试用例文件失败");
        }

        // 插入数据库记录
        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setProblemId(problemId);
        testCase.setInputUrl(inputRelativePath);
        testCase.setOutputUrl(outputRelativePath);
        testCase.setCreateTime(LocalDateTime.now());
        testCase.setUpdateTime(LocalDateTime.now());

        int rows = adminMapper.insertProblemTestCase(testCase);
        if (rows == 0) {
            throw new BusinessException(500, "保存测试用例记录失败");
        }
    }

    /**
     * 为题目添加分类
     */
    private void addProblemCategory(Long problemId, String categoryName) {
        // 尝试查找分类
        Long categoryId = adminMapper.selectCategoryIdByName(categoryName);

        // 如果分类不存在，创建新分类
        if (categoryId == null) {
            LocalDateTime now = LocalDateTime.now();
            int rows = adminMapper.insertCategory(categoryName, now, now);
            if (rows == 0) {
                log.warn("创建分类失败: {}", categoryName);
                return;
            }
            categoryId = adminMapper.selectCategoryIdByName(categoryName);
        }

        // 添加题目分类关联
        if (categoryId != null) {
            adminMapper.insertProblemCategory(problemId, categoryId, LocalDateTime.now());
        }
    }

    /**
     * 安全解析文件路径
     */
    private Path resolveSafePath(String relativePath) {
        Path rootPath = Paths.get(testCaseRoot).toAbsolutePath().normalize();
        Path resolved = rootPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BusinessException(400, "文件路径异常");
        }
        return resolved;
    }

    private String extractField(String content, String fieldName) {
        int startIndex = content.indexOf(fieldName);
        if (startIndex == -1) {
            return "";
        }

        startIndex += fieldName.length();

        String[] possibleNextFields = {"【算法板块】：", "【复杂度分析】：", "【代码风格】：",
                "【题目名称】：", "【题目描述】：",
                "【样例输入1】：", "【样例输出1】：",
                "【样例输入2】：", "【样例输出2】：",
                "【输入格式】：", "【输出格式】："};

        int endIndex = content.length();
        for (String nextField : possibleNextFields) {
            if (!nextField.equals(fieldName)) {
                int nextIndex = content.indexOf(nextField, startIndex);
                if (nextIndex != -1 && nextIndex < endIndex) {
                    endIndex = nextIndex;
                }
            }
        }

        return content.substring(startIndex, endIndex).trim();
    }
}
