package com.itheim.program_platform_backend.interceptor;

import com.itheim.program_platform_backend.mapper.AuthMapper;
import com.itheim.program_platform_backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthMapper authMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取URL
        String url = request.getRequestURI();
        log.info("请求URL: {}", url);

        // 放行 API 文档相关路径
        if (url.equals("/doc.html") ||
                url.startsWith("/webjars/") ||
                url.startsWith("/swagger-resources/") ||
                url.startsWith("/v2/api-docs")) {
            return true;
        }

        // 2.获取请求头中的令牌（token）
        String jwt = request.getHeader("token");

        // 3.判断令牌是否存在，如果不存在，返回错误结果。
        if (jwt == null || jwt.isEmpty()) {
            log.warn("请求头token为空，返回401");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }

        // 4.解析令牌，如果解析失败，返回错误结果。
        try {
            Claims claims = jwtUtil.parseToken(jwt);
            // 5. 检查 Token 是否在数据库中存在（验证是否为最新有效 Token）
            int count = authMapper.countByToken(jwt);
            if (count == 0) {
                log.warn("Token无效或已过期，用户ID: {}", claims.get("userId"));
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
                return false;
            }
            // 可以将用户信息存入request，供后续使用
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role", claims.get("role"));
            log.info("Token验证成功，用户ID: {}", claims.get("userId"));
        } catch (JwtUtil.JwtException e) {
            log.warn("令牌解析失败: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"\" + e.getMessage() + \"\",\"data\":null}");
            return false;
        }

        return true;
    }
}
