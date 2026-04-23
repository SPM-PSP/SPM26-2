package com.itheim.program_platform_backend.service.serviceImpl;

import com.itheim.program_platform_backend.config.JudgeConfig;
import com.itheim.program_platform_backend.domain.dto.JudgeRequest;
import com.itheim.program_platform_backend.domain.dto.JudgeResponse;
import com.itheim.program_platform_backend.enums.JudgeResultEnum;
import com.itheim.program_platform_backend.exception.JudgeException;
import com.itheim.program_platform_backend.service.JudgeService;
import com.itheim.program_platform_backend.utils.CommandExecutor;
import com.itheim.program_platform_backend.utils.FileOperationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeConfig judgeConfig;

    @Override
    public JudgeResponse judge(JudgeRequest request) {
        // 1. 前置检查Docker
        if (!checkDockerStatus()) {
            throw new JudgeException("Docker服务未启动，请先启动Docker Desktop");
        }

        // 2. 生成唯一任务目录（并发隔离核心）
        String taskId = UUID.randomUUID().toString();
        String taskRoot = judgeConfig.getTempTaskRoot() + taskId + "/";
        String dataDir = taskRoot + "data/";
        String logsDir = taskRoot + "logs/";
        log.info("开始评测任务: {}, 临时目录: {}", taskId, taskRoot);

        try {
            // 3. 初始化目录并写入文件
            FileOperationUtil.createDirIfNotExists(dataDir);
            FileOperationUtil.createDirIfNotExists(logsDir);
            FileOperationUtil.writeFile(dataDir + "main.cpp", request.getCode());
            FileOperationUtil.writeFile(dataDir + "input.txt", request.getInput());
            FileOperationUtil.writeFile(dataDir + "answer.txt", request.getAnswer());

            // 4. 复制评测脚本
            String judgeShSource = judgeConfig.getScriptPath() + "judge.sh";
            String judgeShTarget = logsDir + "judge.sh";
            FileOperationUtil.copyFile(judgeShSource, judgeShTarget);

            // 5. 构建Docker命令
            int timeLimit = request.getTimeLimit() != null ? request.getTimeLimit() : judgeConfig.getDefaultTimeLimit();
            String memoryLimit = request.getMemoryLimit() != null ? request.getMemoryLimit() : judgeConfig.getDefaultMemoryLimit();

            String[] dockerCmd = {
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--cap-drop=ALL",
                    "--cpus", "1",
                    "--memory", memoryLimit,
                    "--memory-swap", memoryLimit,
                    "-v", FileOperationUtil.toDockerPath(dataDir) + ":/data:ro",
                    "-v", FileOperationUtil.toDockerPath(logsDir) + ":/logs:rw",
                    "-v", FileOperationUtil.toDockerPath(judgeShTarget) + ":/judge.sh:ro",
                    "--entrypoint", "sh",
                    judgeConfig.getDockerImage(),
                    "/judge.sh", String.valueOf(timeLimit), memoryLimit
            };

            // 6. 执行评测
            CommandExecutor.CommandResult result = CommandExecutor.execute(dockerCmd, judgeConfig.getCommandTimeout());
            JudgeResultEnum resultEnum = JudgeResultEnum.getByExitCode(result.exitCode());
            log.info("任务{}评测完成: {}, 退出码: {}", taskId, resultEnum.getStatus(), result.exitCode());

            // 7. 封装返回结果
            JudgeResponse resp = new JudgeResponse();
            resp.setCode(resultEnum.getExitCode());
            resp.setStatus(resultEnum.getStatus());
            resp.setMessage(resultEnum.getMessage());

            // 根据状态填充对应日志
            switch (resultEnum) {
                case CE -> resp.setCompileLog(FileOperationUtil.readFile(logsDir + "compile.log"));
                case RE -> resp.setRuntimeLog(FileOperationUtil.readFile(logsDir + "runtime.log"));
                case WA -> {
                    resp.setDiffLog(FileOperationUtil.readFile(logsDir + "diff.log"));
                    resp.setUserOutput(FileOperationUtil.readFile(logsDir + "user_output.txt"));
                    resp.setFormattedAnswer(FileOperationUtil.readFile(logsDir + "answer_formatted.txt"));
                    resp.setFormattedOutput(FileOperationUtil.readFile(logsDir + "output_formatted.txt"));
                }
                case AC -> resp.setUserOutput(FileOperationUtil.readFile(logsDir + "user_output.txt"));
            }

            return resp;
        } finally {
            // 8. 自动清理临时文件
            if (!judgeConfig.isKeepTempFiles()) {
                try {
                    FileOperationUtil.deleteDir(taskRoot);
                    log.info("已清理临时目录: {}", taskRoot);
                } catch (Exception e) {
                    log.error("清理临时目录失败: {}", taskRoot, e);
                }
            }
        }
    }

    @Override
    public boolean checkDockerStatus() {
        try {
            return CommandExecutor.execute(new String[]{"docker", "info"}, 5).exitCode() == 0;
        } catch (Exception e) {
            log.error("Docker状态检查失败", e);
            return false;
        }
    }
}