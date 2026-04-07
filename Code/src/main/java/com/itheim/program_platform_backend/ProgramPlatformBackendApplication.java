package com.itheim.program_platform_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itheim.program_platform_backend.mapper")
public class ProgramPlatformBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramPlatformBackendApplication.class, args);
	}

}
