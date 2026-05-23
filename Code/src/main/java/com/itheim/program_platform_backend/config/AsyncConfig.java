package com.itheim.program_platform_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 用于判题等耗时操作的异步执行
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 判题任务线程池
     * 核心参数说明：
     * - corePoolSize: 核心线程数，保持活跃的线程数量
     * - maxPoolSize: 最大线程数，高峰期最多创建的线程数
     * - queueCapacity: 队列容量，等待执行的任务数量
     * - keepAliveSeconds: 线程空闲存活时间
     */
    @Bean(name = "judgeTaskExecutor")
    public Executor judgeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：根据 CPU 核心数设置（建议为 CPU 核心数的 50%-75%）
        // 假设 8 核 CPU，设置为 4
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：最多同时运行的判题任务
        // 考虑到 Docker 容器的资源消耗，不宜设置过大
        int maxPoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量：等待判题的任务队列
        // 当核心线程都在忙时，新任务进入队列等待
        executor.setQueueCapacity(100);
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀，方便日志追踪
        executor.setThreadNamePrefix("judge-task-");
        
        // 拒绝策略：CallerRunsPolicy - 由调用线程执行，起到限流作用
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭：等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("判题任务线程池初始化完成 - 核心线程: {}, 最大线程: {}, 队列容量: {}", 
                corePoolSize, maxPoolSize, executor.getQueueCapacity());
        
        return executor;
    }
}
