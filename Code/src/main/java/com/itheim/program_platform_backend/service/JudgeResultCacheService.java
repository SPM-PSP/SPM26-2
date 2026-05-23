package com.itheim.program_platform_backend.service;

import com.itheim.program_platform_backend.domain.vo.JudgeResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 判题结果缓存服务
 * 用于异步判题模式下缓存判题结果，供前端查询
 */
@Slf4j
@Service
public class JudgeResultCacheService {

    // 使用 ConcurrentHashMap 保证线程安全
    private final Map<Long, JudgeResponseVO> resultCache = new ConcurrentHashMap<>();
    private final Map<Long, String> statusCache = new ConcurrentHashMap<>();

    /**
     * 保存判题结果到缓存
     */
    public void saveResult(Long submissionId, JudgeResponseVO result) {
        resultCache.put(submissionId, result);
        log.info("保存判题结果到缓存: submissionId={}", submissionId);
    }

    /**
     * 获取判题结果
     */
    public JudgeResponseVO getResult(Long submissionId) {
        return resultCache.get(submissionId);
    }

    /**
     * 移除判题结果（清理缓存）
     */
    public void removeResult(Long submissionId) {
        resultCache.remove(submissionId);
        statusCache.remove(submissionId);
        log.info("从缓存移除判题结果: submissionId={}", submissionId);
    }

    /**
     * 更新判题状态
     */
    public void updateStatus(Long submissionId, String status) {
        statusCache.put(submissionId, status);
    }

    /**
     * 获取判题状态
     */
    public String getStatus(Long submissionId) {
        return statusCache.getOrDefault(submissionId, "PENDING");
    }

    /**
     * 清理过期的缓存结果（可选：定时任务调用）
     */
    public void cleanExpiredResults(int expireMinutes) {
        // 这里可以结合 Redis 实现过期清理
        // 当前使用内存缓存，依赖 JVM 垃圾回收
        log.debug("缓存大小: results={}, statuses={}", resultCache.size(), statusCache.size());
    }
}
