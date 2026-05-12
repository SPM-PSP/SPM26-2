package com.itheim.program_platform_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "judge")
public class JudgeConfig {
    private String scriptPath;
    private String tempTaskRoot;
    private int defaultTimeLimit;
    private String defaultMemoryLimit;
    private int commandTimeout;
    private boolean keepTempFiles;

    private Map<String, LanguageConfig> languages;

    @Data
    public static class LanguageConfig {
        private String dockerImage;
        private String scriptFile;
        private String sourceFileName;
    }
}
