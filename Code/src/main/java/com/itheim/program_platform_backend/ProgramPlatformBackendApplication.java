package com.itheim.program_platform_backend;

import com.itheim.program_platform_backend.config.JudgeConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.itheim.program_platform_backend.mapper")
@EnableConfigurationProperties(JudgeConfig.class)
public class ProgramPlatformBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramPlatformBackendApplication.class, args);
	}

}
