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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {
    private final VolcLlmUtil volcLlmUtil;

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

        return response;
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
