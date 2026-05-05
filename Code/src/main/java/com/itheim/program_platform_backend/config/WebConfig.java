package com.itheim.program_platform_backend.config;

import com.itheim.program_platform_backend.interceptor.TokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/algorithm/**" ,"/api/judge/cpp", "/api/v1/auth/login", "/api/v1/auth/register","/api/v1/auth/logout",
                        "/error", "/doc.html", "/webjars/**", "/swagger-resources/**",
                        "/v2/api-docs", "/favicon.ico", "/.well-known/**");
    }

}
