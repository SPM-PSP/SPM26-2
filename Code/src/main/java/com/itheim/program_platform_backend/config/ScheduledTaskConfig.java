package com.itheim.program_platform_backend.config;

import com.itheim.program_platform_backend.service.ResourceMonitorService;
import com.itheim.program_platform_backend.utils.DockerResourceCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 * 用于定期执行系统资源清理和监控任务
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskConfig {
    
    private final ResourceMonitorService resourceMonitorService;
    
    /**
     * 每小时记录一次系统资源使用情况
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void logHourlyResourceUsage() {
        log.info("=== 每小时系统资源监控 ===");
        resourceMonitorService.logResourceUsage();
    }
    
    /**
     * 每30分钟检查一次系统资源，如果过高则清理
     */
    @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    public void checkAndCleanupResources() {
        log.debug("执行定期资源检查和清理...");
        try {
            if (resourceMonitorService.isResourceUsageHigh(75.0, 75.0)) {
                log.warn("定期检测发现系统资源使用过高，执行清理...");
                DockerResourceCleaner.performFullCleanup();
                resourceMonitorService.logResourceUsage();
            }
        } catch (Exception e) {
            log.error("定期资源清理失败", e);
        }
    }
    
    /**
     * 每天凌晨2点执行一次全面清理
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void dailyFullCleanup() {
        log.info("=== 执行每日全面Docker资源清理 ===");
        DockerResourceCleaner.performFullCleanup();
        resourceMonitorService.logResourceUsage();
        log.info("=== 每日全面清理完成 ===");
    }
}