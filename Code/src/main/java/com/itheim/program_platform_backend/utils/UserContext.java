package com.itheim.program_platform_backend.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContext {
    
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return userIdThreadLocal.get();
    }
    
    /**
     * 清除当前用户ID(防止内存泄漏)
     */
    public static void clear() {
        userIdThreadLocal.remove();
        log.debug("清除ThreadLocal中的用户ID");
    }
}
