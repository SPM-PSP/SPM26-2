package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.domain.dto.CodeAnalysisRequest;
import com.itheim.program_platform_backend.domain.dto.CodeAnalysisResponse;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateRequest;
import com.itheim.program_platform_backend.domain.dto.ProblemGenerateResponse;
import com.itheim.program_platform_backend.service.AlgorithmService;
import com.itheim.program_platform_backend.utils.VolcLlmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {
    private final VolcLlmUtil volcLlmUtil;

    /**
     * 代码分析核心逻辑：构造精准提示词调用大模型
     */
    @Override
    public CodeAnalysisResponse analyzeCode(CodeAnalysisRequest request) {
        // 构造代码分析提示词（精准指令确保返回格式可控）
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

        // 调用大模型
        String llmResponse = volcLlmUtil.callLlm(prompt);
        log.info("代码分析响应：{}", llmResponse);

        // 解析响应结果
        CodeAnalysisResponse response = new CodeAnalysisResponse();
        String[] lines = llmResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("【算法板块】：")) {
                response.setPlateCategory(line.replace("【算法板块】：", "").trim());
            } else if (line.startsWith("【复杂度分析】：")) {
                response.setComplexityAnalysis(line.replace("【复杂度分析】：", "").trim());
            } else if (line.startsWith("【代码风格】：")) {
                response.setCodeStyleAnalysis(line.replace("【代码风格】：", "").trim());
            }
        }
        return response;
    }

    /**
     * 题目生成核心逻辑：构造提示词生成标准化算法题
     */
    @Override
    public ProblemGenerateResponse generateProblem(ProblemGenerateRequest request) {
        // 构造题目生成提示词
        String prompt = String.format("""
                请生成一道%s难度的%s算法题（目标语言：%s），要求输出结构化结果：
                1. 题目名称：简洁明了，符合算法题命名规范；
                2. 题目描述：详细说明题目要求、输入约束、数据范围；
                3. 样例输入：给出1-2个典型输入案例；
                4. 样例输出：对应样例输入的输出结果；
                5. 输入格式：说明输入的格式规范；
                6. 输出格式：说明输出的格式规范；
                输出格式要求：
                【题目名称】：xxx
                【题目描述】：xxx
                【样例输入】：xxx
                【样例输出】：xxx
                【输入格式】：xxx
                【输出格式】：xxx
                """, request.getDifficulty(), request.getPlate(), request.getTargetLanguage());

        // 调用大模型
        String llmResponse = volcLlmUtil.callLlm(prompt);
        log.info("题目生成响应：{}", llmResponse);

        // 解析响应结果
        ProblemGenerateResponse response = new ProblemGenerateResponse();
        String[] lines = llmResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("【题目名称】：")) {
                response.setProblemName(line.replace("【题目名称】：", "").trim());
            } else if (line.startsWith("【题目描述】：")) {
                response.setProblemDesc(line.replace("【题目描述】：", "").trim());
            } else if (line.startsWith("【样例输入】：")) {
                response.setSampleInput(line.replace("【样例输入】：", "").trim());
            } else if (line.startsWith("【样例输出】：")) {
                response.setSampleOutput(line.replace("【样例输出】：", "").trim());
            } else if (line.startsWith("【输入格式】：")) {
                response.setInputFormat(line.replace("【输入格式】：", "").trim());
            } else if (line.startsWith("【输出格式】：")) {
                response.setOutputFormat(line.replace("【输出格式】：", "").trim());
            }
        }
        return response;
    }
}