package com.itheim.program_platform_backend.utils;

import com.itheim.program_platform_backend.exception.JudgeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CommandExecutor {

    public static CommandResult execute(String[] command, int timeout) {
        Process process = null;
        try {
            log.debug("执行命令: {}", String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            process = pb.start();

            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();

            // 超时强制销毁
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                log.warn("命令执行超时 ({}s)，强制终止: {}", timeout, String.join(" ", command));
                process.destroyForcibly();
                // 等待进程真正终止
                process.waitFor(5, TimeUnit.SECONDS);
                throw new JudgeException("命令执行超时，已强制终止");
            }

            CommandResult result = new CommandResult(
                    process.exitValue(),
                    IOUtils.toString(stdout, StandardCharsets.UTF_8),
                    IOUtils.toString(stderr, StandardCharsets.UTF_8)
            );
            
            log.debug("命令执行完成 - 退出码: {}", result.exitCode());
            return result;
        } catch (Exception e) {
            log.error("执行系统命令失败: {}", String.join(" ", command), e);
            throw new JudgeException("执行系统命令失败", e);
        } finally {
            if (process != null) {
                try {
                    // 确保进程被销毁
                    if (process.isAlive()) {
                        log.warn("进程仍在运行，强制销毁");
                        process.destroyForcibly();
                        process.waitFor(5, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    log.warn("销毁进程时发生异常: {}", e.getMessage());
                }
            }
        }
    }

    public record CommandResult(int exitCode, String stdout, String stderr) {}
}