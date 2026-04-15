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
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            process = pb.start();

            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();

            // 超时强制销毁
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new JudgeException("命令执行超时，已强制终止");
            }

            return new CommandResult(
                    process.exitValue(),
                    IOUtils.toString(stdout, StandardCharsets.UTF_8),
                    IOUtils.toString(stderr, StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new JudgeException("执行系统命令失败", e);
        } finally {
            if (process != null) process.destroy();
        }
    }

    public record CommandResult(int exitCode, String stdout, String stderr) {}
}