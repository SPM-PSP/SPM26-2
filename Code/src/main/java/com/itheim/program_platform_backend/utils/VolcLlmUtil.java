package com.itheim.program_platform_backend.utils;

import com.itheim.program_platform_backend.config.VolcLlmConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class VolcLlmUtil {
    private final VolcLlmConfig volcLlmConfig;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    public String callLlm(String prompt) {
        String requestBody = buildRequestJson(prompt);

        log.info("调用火山大模型 - URL: {}", volcLlmConfig.getBaseUrl() + "/chat/completions");
        log.info("调用火山大模型 - Model: {}", volcLlmConfig.getModel());
        log.debug("调用火山大模型 - 请求体: {}", requestBody);

        Request request = new Request.Builder()
                .url(volcLlmConfig.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + volcLlmConfig.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";

            if (!response.isSuccessful()) {
                log.error("火山大模型调用失败 - 响应码: {}, 响应体: {}", response.code(), responseBody);
                log.error("请求URL: {}", request.url());
                log.error("请求Model: {}", volcLlmConfig.getModel());
                throw new RuntimeException(String.format("大模型调用失败 [HTTP %d]: %s", response.code(), responseBody));
            }

            log.debug("火山大模型调用成功 - 响应体: {}", responseBody);
            return parseResponseContent(responseBody);
        } catch (IOException e) {
            log.error("大模型调用IO异常 - URL: {}, Model: {}",
                    volcLlmConfig.getBaseUrl() + "/chat/completions",
                    volcLlmConfig.getModel(), e);
            throw new RuntimeException("大模型调用异常: " + e.getMessage(), e);
        }
    }

    private String buildRequestJson(String prompt) {
        try {
            JsonNode requestNode = objectMapper.createObjectNode()
                    .put("model", volcLlmConfig.getModel())
                    .put("temperature", 0.7)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", prompt)));
            return objectMapper.writeValueAsString(requestNode);
        } catch (Exception e) {
            log.error("构建请求JSON失败", e);
            throw new RuntimeException("请求参数构建失败");
        }
    }

    private String parseResponseContent(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.get("choices").get(0).get("message").get("content").asText().trim();
        } catch (Exception e) {
            log.error("解析响应失败 - 响应体: {}", responseBody, e);
            throw new RuntimeException("响应解析失败: " + e.getMessage());
        }
    }
}
