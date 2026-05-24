package com.itheim.program_platform_backend.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Docker资源清理工具类
 * 用于定期清理可能残留的Docker容器和镜像，防止资源泄漏
 */
@Slf4j
public class DockerResourceCleaner {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * 启动定期清理任务
     * @param intervalMinutes 清理间隔（分钟）
     */
    public static void startPeriodicCleanup(int intervalMinutes) {
        log.info("启动Docker资源定期清理任务，间隔: {} 分钟", intervalMinutes);
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("执行定期Docker资源清理...");
                cleanupStoppedContainers();
                cleanupUnusedImages();
                cleanupBuildCache();
                log.debug("定期Docker资源清理完成");
            } catch (Exception e) {
                log.error("定期Docker资源清理失败", e);
            }
        }, 5, intervalMinutes, TimeUnit.MINUTES); // 首次延迟5分钟执行
    }
    
    /**
     * 清理已停止的容器
     */
    public static void cleanupStoppedContainers() {
        try {
            String[] cmd = {"docker", "container", "prune", "-f"};
            CommandExecutor.CommandResult result = CommandExecutor.execute(cmd, 30);
            if (result.exitCode() == 0) {
                log.debug("已清理停止的Docker容器: {}", result.stdout());
            } else {
                log.warn("清理停止的Docker容器失败: {}", result.stderr());
            }
        } catch (Exception e) {
            log.error("清理停止的Docker容器时发生异常", e);
        }
    }
    
    /**
     * 清理未使用的镜像
     */
    public static void cleanupUnusedImages() {
        try {
            String[] cmd = {"docker", "image", "prune", "-f"};
            CommandExecutor.CommandResult result = CommandExecutor.execute(cmd, 30);
            if (result.exitCode() == 0) {
                log.debug("已清理未使用的Docker镜像: {}", result.stdout());
            } else {
                log.warn("清理未使用的Docker镜像失败: {}", result.stderr());
            }
        } catch (Exception e) {
            log.error("清理未使用的Docker镜像时发生异常", e);
        }
    }
    
    /**
     * 清理构建缓存
     */
    public static void cleanupBuildCache() {
        try {
            String[] cmd = {"docker", "builder", "prune", "-f"};
            CommandExecutor.CommandResult result = CommandExecutor.execute(cmd, 30);
            if (result.exitCode() == 0) {
                log.debug("已清理Docker构建缓存: {}", result.stdout());
            } else {
                log.warn("清理Docker构建缓存失败: {}", result.stderr());
            }
        } catch (Exception e) {
            log.error("清理Docker构建缓存时发生异常", e);
        }
    }
    
    /**
     * 立即执行一次全面清理
     */
    public static void performFullCleanup() {
        log.info("执行Docker资源全面清理...");
        cleanupStoppedContainers();
        cleanupUnusedImages();
        cleanupBuildCache();
        log.info("Docker资源全面清理完成");
    }
    
    /**
     * 关闭清理器
     */
    public static void shutdown() {
        log.info("关闭Docker资源清理器...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Docker资源清理器已关闭");
    }
}