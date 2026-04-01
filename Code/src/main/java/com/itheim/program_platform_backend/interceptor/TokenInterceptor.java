package com.itheim.program_platform_backend.interceptor;

import com.itheim.program_platform_backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取URL
        String url = request.getRequestURI();

        /*//2.判断请求url中是否包含login，如果包含，说明是登录操作，放行。
        if (url.contains("login")) {
            log.info("登录操作，放行");
            return true;
        }*/

        //3.获取请求头中的令牌（token）
        String jwt = request.getHeader("token");

        //4.判断令牌是否存在，如果不存在，返回错误结果。
        if (jwt == null|| jwt.isEmpty()) {
            log.info("请求头token为空，返回错误结果");
            response.setStatus(401);
            return false;
        }

        //5.解析令牌，如果解析失败，返回错误结果。
        try {
            JwtUtil.parseToken(jwt);
        } catch (JwtUtil.JwtException e) {
            e.printStackTrace();
            log.info("令牌解析失败，返回错误结果");
            response.setStatus(401);
            return false;
        }

        return true;
    }
    }
