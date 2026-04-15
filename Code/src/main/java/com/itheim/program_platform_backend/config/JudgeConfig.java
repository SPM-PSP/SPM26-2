package com.itheim.program_platform_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "judge")
public class JudgeConfig {
    private String scriptPath;
    private String tempTaskRoot;
    private String dockerImage;
    private int defaultTimeLimit;
    private String defaultMemoryLimit;
    private int commandTimeout;
    private boolean keepTempFiles;
}