package com.itheim.program_platform_backend.controller;

import com.itheim.program_platform_backend.domain.po.Result;
import com.itheim.program_platform_backend.service.ResourceMonitorService;
import com.itheim.program_platform_backend.utils.DockerResourceCleaner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统资源监控控制器
 * 提供系统资源使用情况的查询和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Api(tags = "系统资源监控")
public class SystemResourceController {
    
    private final ResourceMonitorService resourceMonitorService;
    
    @ApiOperation("获取系统资源使用情况")
    @GetMapping("/resources")
    public Result<Map<String, Object>> getSystemResources() {
        try {
            Map<String, Object> resourceUsage = resourceMonitorService.getSystemResourceUsage();
            return Result.success(resourceUsage);
        } catch (Exception e) {
            log.error("获取系统资源使用情况失败", e);
            return new Result<>(500, "获取系统资源信息失败: " + e.getMessage(), null);
        }
    }
    
    @ApiOperation("手动触发Docker资源清理")
    @PostMapping("/docker/cleanup")
    public Result<Void> triggerDockerCleanup() {
        try {
            log.info("手动触发Docker资源清理");
            DockerResourceCleaner.performFullCleanup();
            return Result.success();
        } catch (Exception e) {
            log.error("Docker资源清理失败", e);
            return new Result<>(500, "Docker资源清理失败: " + e.getMessage(), null);
        }
    }
    
    @ApiOperation("记录当前系统资源使用情况到日志")
    @PostMapping("/resources/log")
    public Result<Void> logCurrentResources() {
        try {
            resourceMonitorService.logResourceUsage();
            return Result.success();
        } catch (Exception e) {
            log.error("记录系统资源使用情况失败", e);
            return new Result<>(500, "记录系统资源信息失败: " + e.getMessage(), null);
        }
    }
    
    @ApiOperation("检查系统资源是否过高")
    @GetMapping("/resources/check")
    public Result<Map<String, Object>> checkResourceUsage(
            @RequestParam(defaultValue = "80.0") double cpuThreshold,
            @RequestParam(defaultValue = "80.0") double memoryThreshold) {
        try {
            boolean isHigh = resourceMonitorService.isResourceUsageHigh(cpuThreshold, memoryThreshold);
            Map<String, Object> result = resourceMonitorService.getSystemResourceUsage();
            result.put("isResourceUsageHigh", isHigh);
            result.put("cpuThreshold", cpuThreshold);
            result.put("memoryThreshold", memoryThreshold);
            
            if (isHigh) {
                return new Result<>(200, "系统资源使用过高，建议清理", result);
            } else {
                return Result.success(result);
            }
        } catch (Exception e) {
            log.error("检查系统资源使用情况失败", e);
            return new Result<>(500, "检查系统资源信息失败: " + e.getMessage(), null);
        }
    }
}