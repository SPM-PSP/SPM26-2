package com.itheim.program_platform_backend.config;

import com.itheim.program_platform_backend.utils.DockerResourceCleaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 优化判题任务线程池配置，防止资源泄漏
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 判题任务线程池
     * 核心参数说明：
     * - corePoolSize: 核心线程数，保持活跃的线程数量
     * - maxPoolSize: 最大线程数，高峰期最多创建的线程数
     * - queueCapacity: 队列容量，等待执行的任务数量
     * - keepAliveSeconds: 线程空闲存活时间
     * 
     * 优化策略：
     * - Docker 容器 CPU 限制为 0.5 核，因此可以支持更多并发
     * - 理论最大并发 = CPU核心数 / 单容器CPU配额 = 8 / 0.5 = 16
     */
    @Bean(name = "judgeTaskExecutor")
    public Executor judgeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 获取系统 CPU 核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // 单个容器的 CPU 配额（与 JudgeServiceImpl 中的 dockerCpus 保持一致）
        double containerCpuQuota = 0.5;
        
        // 计算理论最大并发数
        int theoreticalMaxConcurrency = (int) (cpuCores / containerCpuQuota);
        
        // 核心线程数：理论并发的 50%-60%，保证基础吞吐
        int corePoolSize = Math.max(4, (int) (theoreticalMaxConcurrency * 0.6));
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：理论并发的 80%-90%，预留缓冲空间
        // 避免 100% 满载导致上下文切换开销过大
        int maxPoolSize = Math.max(8, (int) (theoreticalMaxConcurrency * 0.9));
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
        
        // 允许核心线程超时回收
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        
        log.info("判题任务线程池初始化完成 - 核心线程: {}, 最大线程: {}, 队列容量: {}", 
                corePoolSize, maxPoolSize, executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 启动Docker资源定期清理任务
     * 每10分钟清理一次残留的Docker资源
     */
    @Bean
    public DockerResourceCleaner dockerResourceCleaner() {
        log.info("初始化Docker资源清理器...");
        DockerResourceCleaner.startPeriodicCleanup(10); // 每10分钟清理一次
        return new DockerResourceCleaner();
    }
    
    /**
     * 应用关闭时的清理操作
     */
    @PreDestroy
    public void cleanup() {
        log.info("应用关闭，执行资源清理...");
        DockerResourceCleaner.shutdown();
        log.info("资源清理完成");
    }
}
