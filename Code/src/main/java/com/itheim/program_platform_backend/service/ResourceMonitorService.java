package com.itheim.program_platform_backend.service;

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统资源监控服务
 * 用于监控CPU、内存、Docker等资源使用情况
 */
@Slf4j
@Service
public class ResourceMonitorService {
    
    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    /**
     * 获取系统资源使用情况
     */
    public Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> resourceInfo = new HashMap<>();
        
        // JVM内存使用
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        resourceInfo.put("jvm.usedMemoryMB", usedMemory / 1024 / 1024);
        resourceInfo.put("jvm.totalMemoryMB", totalMemory / 1024 / 1024);
        resourceInfo.put("jvm.maxMemoryMB", maxMemory / 1024 / 1024);
        resourceInfo.put("jvm.memoryUsagePercent", (usedMemory * 100.0) / maxMemory);
        
        // CPU使用率
        double cpuLoad = osBean.getSystemCpuLoad();
        if (cpuLoad >= 0) {
            resourceInfo.put("system.cpuLoadPercent", cpuLoad * 100);
        } else {
            resourceInfo.put("system.cpuLoadPercent", -1); // 不可用
        }
        
        // 系统负载
        double systemLoadAverage = osBean.getSystemLoadAverage();
        if (systemLoadAverage >= 0) {
            resourceInfo.put("system.loadAverage", systemLoadAverage);
        } else {
            resourceInfo.put("system.loadAverage", -1); // 不可用
        }
        
        // Docker容器数量
        try {
            int runningContainers = getRunningContainerCount();
            int totalContainers = getTotalContainerCount();
            resourceInfo.put("docker.runningContainers", runningContainers);
            resourceInfo.put("docker.totalContainers", totalContainers);
        } catch (Exception e) {
            log.warn("获取Docker容器数量失败: {}", e.getMessage());
            resourceInfo.put("docker.runningContainers", -1);
            resourceInfo.put("docker.totalContainers", -1);
        }
        
        return resourceInfo;
    }
    
    /**
     * 获取运行中的容器数量
     */
    private int getRunningContainerCount() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "--format", "{{.ID}}");
        pb.redirectErrorStream(false);
        Process process = pb.start();
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        
        process.waitFor();
        return count;
    }
    
    /**
     * 获取总容器数量（包括停止的）
     */
    private int getTotalContainerCount() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "-a", "--format", "{{.ID}}");
        pb.redirectErrorStream(false);
        Process process = pb.start();
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        
        process.waitFor();
        return count;
    }
    
    /**
     * 检查系统资源是否超过阈值
     * @return true 如果资源使用过高
     */
    public boolean isResourceUsageHigh(double cpuThresholdPercent, double memoryThresholdPercent) {
        Map<String, Object> usage = getSystemResourceUsage();
        
        Double cpuLoad = (Double) usage.get("system.cpuLoadPercent");
        Double memoryUsage = (Double) usage.get("jvm.memoryUsagePercent");
        
        boolean highCpu = cpuLoad != null && cpuLoad > cpuThresholdPercent;
        boolean highMemory = memoryUsage != null && memoryUsage > memoryThresholdPercent;
        
        if (highCpu || highMemory) {
            log.warn("系统资源使用过高 - CPU: {}%, 内存: {}%", 
                    cpuLoad != null ? String.format("%.2f", cpuLoad) : "N/A",
                    memoryUsage != null ? String.format("%.2f", memoryUsage) : "N/A");
        }
        
        return highCpu || highMemory;
    }
    
    /**
     * 记录当前资源使用情况到日志
     */
    public void logResourceUsage() {
        Map<String, Object> usage = getSystemResourceUsage();
        log.info("系统资源使用情况 - JVM内存: {}/{} MB ({}%), CPU负载: {}%, Docker容器: {}/{}",
                usage.get("jvm.usedMemoryMB"),
                usage.get("jvm.maxMemoryMB"),
                String.format("%.2f", usage.get("jvm.memoryUsagePercent")),
                usage.get("system.cpuLoadPercent") != null && (Double) usage.get("system.cpuLoadPercent") >= 0 
                    ? String.format("%.2f", usage.get("system.cpuLoadPercent")) : "N/A",
                usage.get("docker.runningContainers"),
                usage.get("docker.totalContainers"));
    }
}
