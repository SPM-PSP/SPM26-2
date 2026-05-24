package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.config.JudgeConfig;
import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.enums.JudgeResultEnum;
import com.itheim.program_platform_backend.exception.JudgeException;
import com.itheim.program_platform_backend.service.JudgeService;
import com.itheim.program_platform_backend.service.ResourceMonitorService;
import com.itheim.program_platform_backend.utils.CommandExecutor;
import com.itheim.program_platform_backend.utils.DockerResourceCleaner;
import com.itheim.program_platform_backend.utils.FileOperationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeConfig judgeConfig;
    private final ResourceMonitorService resourceMonitorService;
    
    // Docker 状态标志（定时更新）
    private final AtomicBoolean dockerAvailable = new AtomicBoolean(false);
    
    // 定时检查 Docker 状态的调度器
    private final ScheduledExecutorService dockerStatusScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "docker-status-checker");
        t.setDaemon(true);
        return t;
    });

    /**
     * 应用启动时检查 Docker 状态并启动定时检查任务
     */
    @PostConstruct
    public void init() {
        log.info("========== 初始化判题服务 ==========");
        
        // 首次检查
        updateDockerStatus();
        
        // 启动定时检查任务，每3秒检查一次（提高检测频率，减少误判）
        dockerStatusScheduler.scheduleAtFixedRate(
            this::updateDockerStatus,
            3,   // 首次延迟3秒
            3,   // 每隔3秒检查一次（原10秒，缩短以提高响应速度）
            TimeUnit.SECONDS
        );
        
        log.info("✅ Docker 状态定时检查任务已启动（每3秒检查一次，使用轻量级 docker version 命令）");
        log.info("====================================");
    }
    
    /**
     * 更新 Docker 状态
     */
    private void updateDockerStatus() {
        boolean status = checkDockerStatus();
        boolean previousStatus = dockerAvailable.getAndSet(status);
        
        // 只在状态变化时打印日志
        if (status != previousStatus) {
            if (status) {
                log.info("✅ Docker 服务已启动");
            } else {
                log.warn("⚠️ Docker 服务未启动或已停止");
            }
        }
    }

    @Override
    public JudgeResponse judge(JudgeRequest request) {
        // 使用定时检查的 Docker 状态（每3秒更新一次）
        // 高并发优化：如果检测到 Docker 不可用，进行短暂等待后重试（最多3次）
        boolean dockerReady = dockerAvailable.get();
        if (!dockerReady) {
            log.warn("Docker 状态标志为 false，尝试重新检查...");
            // 立即重新检查一次，避免因为定时检查延迟导致的误判
            dockerReady = checkDockerStatus();
            if (dockerReady) {
                log.info("✅ Docker 重新检查通过，继续判题");
                dockerAvailable.set(true); // 更新标志位
            } else {
                // 仍然失败，等待1秒后再次尝试
                try {
                    Thread.sleep(1000);
                    dockerReady = checkDockerStatus();
                    if (dockerReady) {
                        log.info("✅ Docker 重试成功，继续判题");
                        dockerAvailable.set(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!dockerReady) {
            throw new JudgeException("Docker服务未启动，请先启动Docker Desktop");
        }
        
        // 检查系统资源使用情况，如果过高则进行清理
        checkAndCleanupResources();
        
        log.info("========== 开始判题 ==========");
        String languageKey = resolveLanguageKey(request.getLanguage());
        log.info("语言: {} (resolved: {})", request.getLanguage(), languageKey);
        log.info("代码长度: {} 字符", request.getCode() != null ? request.getCode().length() : 0);
        log.info("输入: {}", request.getInput());
        log.info("答案: {}", request.getAnswer());

        JudgeConfig.LanguageConfig langConfig = judgeConfig.getLanguages().get(languageKey);

        if (langConfig == null) {
            log.error("不支持的语言: {} (key={})", request.getLanguage(), languageKey);
            throw new JudgeException("不支持的编程语言: " + request.getLanguage());
        }
        log.info("语言配置: image={}, script={}, sourceFile={}",
                langConfig.getDockerImage(),
                langConfig.getScriptFile(),
                langConfig.getSourceFileName());

        validateAndNormalizeCode(request, languageKey);

        String taskId = UUID.randomUUID().toString();
        String taskRoot = judgeConfig.getTempTaskRoot() + taskId + "/";
        String dataDir = taskRoot + "data/";
        String logsDir = taskRoot + "logs/";
        log.info("任务ID: {}", taskId);
        log.info("临时目录: {}", taskRoot);
        log.info("数据目录: {}", dataDir);
        log.info("日志目录: {}", logsDir);

        try {

            log.info("创建目录...");
            FileOperationUtil.createDirIfNotExists(dataDir);
            FileOperationUtil.createDirIfNotExists(logsDir);

            String sourceFileName = langConfig.getSourceFileName();
            log.info("写入源代码文件: {}", dataDir + sourceFileName);
            FileOperationUtil.writeFile(dataDir + sourceFileName, request.getCode());
            log.info("写入输入文件: {}", dataDir + "input.txt");
            FileOperationUtil.writeFile(dataDir + "input.txt", request.getInput());
            log.info("写入答案文件: {}", dataDir + "answer.txt");
            FileOperationUtil.writeFile(dataDir + "answer.txt", request.getAnswer());

            String judgeShSource = judgeConfig.getScriptPath() + langConfig.getScriptFile();
            String judgeShTarget = logsDir + langConfig.getScriptFile();
            log.info("脚本源路径: {}", judgeShSource);
            log.info("脚本目标路径: {}", judgeShTarget);

            validateScriptFile(judgeShSource);
            log.info("复制判题脚本...");
            FileOperationUtil.copyFile(judgeShSource, judgeShTarget);

            int timeLimit = request.getTimeLimit() != null ? request.getTimeLimit() : judgeConfig.getDefaultTimeLimit();
            String memoryLimit = request.getMemoryLimit() != null ? request.getMemoryLimit() : judgeConfig.getDefaultMemoryLimit();
            
            // 将毫秒转换为秒传递给判题脚本
            int timeLimitSeconds = (int) Math.ceil(timeLimit / 1000.0);
            log.info("时间限制: {}ms ({}s), 内存限制: {}", timeLimit, timeLimitSeconds, memoryLimit);

            // 将内存限制转换为 KB（用于传递给判题脚本）
            int memoryLimitKB = parseMemoryLimitToKB(memoryLimit);
            log.info("内存限制转换: {} -> {} KB", memoryLimit, memoryLimitKB);

            // Docker 容器使用固定的较大内存上限，确保编译能通过
            // 真正的内存限制由判题脚本中的 ulimit 控制
            String dockerMemoryLimit = "512m";
            
            // CPU 配额优化：降低单容器CPU限制以提升并发能力
            // 0.5 CPU = 允许使用 50% 的单核性能，适合 IO 密集型判题任务
            // 可根据实际负载调整：0.25(更高并发) ~ 1.0(更快单任务)
            String dockerCpus = "0.5";
            
            log.info("Docker 容器内存限制: {} (编译用)", dockerMemoryLimit);
            log.info("Docker 容器 CPU 限制: {} 核", dockerCpus);
            log.info("程序运行内存限制: {} KB (ulimit 控制)", memoryLimitKB);

            String dockerDataPath = FileOperationUtil.toDockerPath(dataDir);
            String dockerLogsPath = FileOperationUtil.toDockerPath(logsDir);
            String dockerScriptPath = FileOperationUtil.toDockerPath(judgeShTarget);

            String[] dockerCmd = {
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--cap-drop=ALL",
                    "--cpus", dockerCpus,  // 使用动态 CPU 配额
                    "--memory", dockerMemoryLimit,
                    "--memory-swap", dockerMemoryLimit,
                    "--label", "judge-task=" + taskId,
                    "--label", "judge-type=code-evaluation",
                    "-v", dockerDataPath + ":/data:ro",
                    "-v", dockerLogsPath + ":/logs:rw",
                    "-v", dockerScriptPath + ":/" + langConfig.getScriptFile() + ":ro",
                    "--entrypoint", "sh",
                    langConfig.getDockerImage(),
                    "/" + langConfig.getScriptFile(), String.valueOf(timeLimitSeconds), String.valueOf(memoryLimitKB)
            };

            log.info("========== 执行 Docker 命令 ==========");
            log.info("完整命令: {}", String.join(" ", dockerCmd));

            CommandExecutor.CommandResult result = CommandExecutor.execute(dockerCmd, judgeConfig.getCommandTimeout());

            log.info("Docker 执行结果 - 退出码: {}", result.exitCode());
            log.info("标准输出: {}", result.stdout());
            log.info("错误输出: {}", result.stderr());

            JudgeResultEnum resultEnum = JudgeResultEnum.getByExitCode(result.exitCode());
            log.info("评测结果: {} ({})", resultEnum.getStatus(), resultEnum.getMessage());

            JudgeResponse resp = new JudgeResponse();
            resp.setCode(resultEnum.getExitCode());
            resp.setStatus(resultEnum.getStatus());
            resp.setMessage(resultEnum.getMessage());

            switch (resultEnum) {
                case CE -> {
                    log.info("编译错误，读取编译日志");
                    resp.setCompileLog(FileOperationUtil.readFile(logsDir + "compile.log"));
                }
                case RE -> {
                    log.info("运行时错误，读取运行日志");
                    resp.setRuntimeLog(FileOperationUtil.readFile(logsDir + "runtime.log"));
                }
                case TLE -> {
                    log.info("时间超限，读取运行日志");
                    resp.setRuntimeLog(FileOperationUtil.readFile(logsDir + "runtime.log"));
                }
                case MLE -> {
                    log.info("内存超限，读取运行日志");
                    resp.setRuntimeLog(FileOperationUtil.readFile(logsDir + "runtime.log"));
                }
                case WA -> {
                    log.info("答案错误，读取差异日志");
                    resp.setDiffLog(FileOperationUtil.readFile(logsDir + "diff.log"));
                    resp.setUserOutput(FileOperationUtil.readFile(logsDir + "user_output.txt"));
                    resp.setFormattedAnswer(FileOperationUtil.readFile(logsDir + "answer_formatted.txt"));
                    resp.setFormattedOutput(FileOperationUtil.readFile(logsDir + "output_formatted.txt"));
                }
                case AC -> {
                    log.info("答案正确");
                    resp.setUserOutput(FileOperationUtil.readFile(logsDir + "user_output.txt"));
                }
                case SYSTEM_ERROR -> {
                    log.error("系统错误，Docker stderr: {}", result.stderr());
                    resp.setMessage("系统内部错误: " + result.stderr());
                }
            }

            // 解析运行时间和内存
            parseRuntimeAndMemory(logsDir, resp);

            log.info("========== 判题完成 ==========");
            return resp;
        } catch (JudgeException e) {
            log.error("判题异常: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("判题未知异常", e);
            throw new JudgeException("判题系统异常: " + e.getMessage(), e);
        } finally {
            // 确保清理临时目录
            if (!judgeConfig.isKeepTempFiles()) {
                try {
                    log.info("清理临时目录: {}", taskRoot);
                    FileOperationUtil.deleteDir(taskRoot);
                    log.info("临时目录已清理");
                } catch (Exception e) {
                    log.error("清理临时目录失败: {}", taskRoot, e);
                }
            }
            
            // 注意：不再手动删除 Docker 容器
            // 原因：
            // 1. 已使用 --rm 参数，容器会自动清理
            // 2. 高并发下手动删除会与 --rm 产生竞态条件
            // 3. 错误信息："container is marked for removal and cannot be started"
            // 4. 定时任务（每10分钟）会清理残留容器作为兜底
        }
    }

    /**
     * 将前端/文档中的语言写法映射为 application.yml 中的 key（cpp / java / python）。
     */
    private String resolveLanguageKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "cpp";
        }
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        if ("c++".equals(s) || "cpp".equals(s) || "cxx".equals(s) || "cplusplus".equals(s)) {
            return "cpp";
        }
        if ("java".equals(s)) {
            return "java";
        }
        if ("python".equals(s) || "py".equals(s) || "python3".equals(s)) {
            return "python";
        }
        return s;
    }

    private void validateAndNormalizeCode(JudgeRequest request, String languageKey) {
        String code = request.getCode();

        if (code == null || code.trim().isEmpty()) {
            throw new JudgeException("代码不能为空");
        }

        switch (languageKey) {
            case "cpp":
                if (!code.contains("#include") && !code.contains("main")) {
                    log.warn("C++ 代码可能不完整");
                }
                break;
            case "java":
                if (!code.contains("class") || !code.contains("main")) {
                    throw new JudgeException("Java 代码必须包含 class 和 main 方法");
                }
                if (!code.contains("class Main")) {
                    log.warn("Java 类名应该是 Main，当前代码可能无法编译");
                }
                break;
            case "python":
                if (!code.contains("def") && !code.contains("print") && !code.contains("input")) {
                    log.warn("Python 代码看起来可能不完整");
                }
                break;
            default:
                throw new JudgeException("不支持的语言: " + languageKey);
        }

        log.debug("代码验证通过，长度: {} 字符", code.length());
    }

    private void validateScriptFile(String scriptPath) {
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            String absolutePath = scriptFile.getAbsolutePath();
            log.error("判题脚本不存在 - 配置路径: {}, 绝对路径: {}", scriptPath, absolutePath);
            throw new JudgeException("判题脚本不存在: " + scriptPath + "\n请检查 application.yml 中 judge.script-path 配置是否正确");
        }
        if (!scriptFile.canRead()) {
            throw new JudgeException("判题脚本不可读: " + scriptPath);
        }
        log.debug("判题脚本验证通过: {}", scriptPath);
    }

    /**
     * 将内存限制字符串转换为 KB（整数）
     * 支持格式: "256m", "500M", "1g", "1G", "524288k", "65536" (纯数字默认为KB)
     * 注意：数据库中存储的 memoryLimit 字段单位是 KB
     */
    private int parseMemoryLimitToKB(String memoryLimit) {
        if (memoryLimit == null || memoryLimit.isEmpty()) {
            return 256 * 1024; // 默认 256MB = 262144 KB
        }

        memoryLimit = memoryLimit.trim().toLowerCase();

        try {
            if (memoryLimit.endsWith("g")) {
                // GB 转 KB
                double gb = Double.parseDouble(memoryLimit.substring(0, memoryLimit.length() - 1));
                return (int) (gb * 1024 * 1024);
            } else if (memoryLimit.endsWith("m")) {
                // MB 转 KB
                double mb = Double.parseDouble(memoryLimit.substring(0, memoryLimit.length() - 1));
                return (int) (mb * 1024);
            } else if (memoryLimit.endsWith("k")) {
                // KB（去掉后缀直接返回）
                return Integer.parseInt(memoryLimit.substring(0, memoryLimit.length() - 1));
            } else {
                // 纯数字，数据库中存储的单位就是 KB，直接返回
                return Integer.parseInt(memoryLimit);
            }
        } catch (NumberFormatException e) {
            log.warn("内存限制格式解析失败: {}, 使用默认值 256MB", memoryLimit);
            return 256 * 1024;
        }
    }

    /**
     * 从判题脚本生成的日志文件中解析运行时间和内存
     * 判题脚本会将运行时间(ms)写入 runtime_ms.txt，内存(KB)写入 memory_kb.txt
     */
    private void parseRuntimeAndMemory(String logsDir, JudgeResponse resp) {
        try {
            // 读取运行时间
            String runtimeStr = FileOperationUtil.readFile(logsDir + "runtime_ms.txt");
            if (runtimeStr != null && !runtimeStr.trim().isEmpty()) {
                resp.setRunTime(Integer.parseInt(runtimeStr.trim()));
                log.info("解析到运行时间: {} ms", resp.getRunTime());
            }

            // 读取内存使用
            String memoryStr = FileOperationUtil.readFile(logsDir + "memory_kb.txt");
            if (memoryStr != null && !memoryStr.trim().isEmpty()) {
                resp.setMemory(Integer.parseInt(memoryStr.trim()));
                log.info("解析到内存使用: {} KB", resp.getMemory());
            }
        } catch (Exception e) {
            log.warn("解析运行时间和内存失败: {}", e.getMessage());
            // 设置默认值
            if (resp.getRunTime() == null) {
                resp.setRunTime(0);
            }
            if (resp.getMemory() == null) {
                resp.setMemory(0);
            }
        }
    }

    @Override
    public boolean checkDockerStatus() {
        try {
            log.debug("检查 Docker 状态...");
            // 使用 docker version 替代 docker info，更轻量快速（快5-10倍）
            CommandExecutor.CommandResult result = CommandExecutor.execute(new String[]{"docker", "version", "--format", "{{.Server.Version}}"}, 3);
            boolean isRunning = result.exitCode() == 0;
            if (!isRunning) {
                log.warn("Docker 状态检查失败，退出码: {}, 错误: {}", result.exitCode(), result.stderr());
            }
            return isRunning;
        } catch (Exception e) {
            log.warn("Docker状态检查异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查系统资源使用情况，如果过高则进行清理
     */
    private void checkAndCleanupResources() {
        try {
            // 检查资源使用情况（CPU和内存超过80%则认为过高）
            if (resourceMonitorService.isResourceUsageHigh(80.0, 80.0)) {
                log.warn("检测到系统资源使用过高，执行Docker资源清理...");
                DockerResourceCleaner.performFullCleanup();
                log.info("Docker资源清理完成，继续判题任务");
                
                // 记录清理后的资源使用情况
                resourceMonitorService.logResourceUsage();
            }
        } catch (Exception e) {
            log.error("资源检查和清理过程中发生异常，但不影响判题: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 应用关闭时停止定时检查任务
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭 Docker 状态定时检查任务...");
        dockerStatusScheduler.shutdown();
        try {
            if (!dockerStatusScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                dockerStatusScheduler.shutdownNow();
                log.warn("强制关闭 Docker 状态检查任务");
            } else {
                log.info("Docker 状态检查任务已优雅关闭");
            }
        } catch (InterruptedException e) {
            dockerStatusScheduler.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("关闭 Docker 状态检查任务时被中断");
        }
    }
}
