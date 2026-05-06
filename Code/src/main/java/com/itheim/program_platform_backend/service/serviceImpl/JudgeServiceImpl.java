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

import java.io.File;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeConfig judgeConfig;

    @Override
    public JudgeResponse judge(JudgeRequest request) {
        log.info("========== 开始判题 ==========");
        log.info("语言: {}", request.getLanguage());
        log.info("代码长度: {} 字符", request.getCode() != null ? request.getCode().length() : 0);
        log.info("输入: {}", request.getInput());
        log.info("答案: {}", request.getAnswer());

        if (!checkDockerStatus()) {
            log.error("Docker 服务未启动");
            throw new JudgeException("Docker服务未启动，请先启动Docker Desktop");
        }
        log.info("Docker 服务正常");

        String language = request.getLanguage().toLowerCase();
        JudgeConfig.LanguageConfig langConfig = judgeConfig.getLanguages().get(language);

        if (langConfig == null) {
            log.error("不支持的语言: {}", request.getLanguage());
            throw new JudgeException("不支持的编程语言: " + request.getLanguage());
        }
        log.info("语言配置: image={}, script={}, sourceFile={}",
                langConfig.getDockerImage(),
                langConfig.getScriptFile(),
                langConfig.getSourceFileName());

        validateAndNormalizeCode(request);

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
            log.info("时间限制: {}s, 内存限制: {}", timeLimit, memoryLimit);

            String dockerDataPath = FileOperationUtil.toDockerPath(dataDir);
            String dockerLogsPath = FileOperationUtil.toDockerPath(logsDir);
            String dockerScriptPath = FileOperationUtil.toDockerPath(judgeShTarget);

            String[] dockerCmd = {
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--cap-drop=ALL",
                    "--cpus", "1",
                    "--memory", memoryLimit,
                    "--memory-swap", memoryLimit,
                    "-v", dockerDataPath + ":/data:ro",
                    "-v", dockerLogsPath + ":/logs:rw",
                    "-v", dockerScriptPath + ":/" + langConfig.getScriptFile() + ":ro",
                    "--entrypoint", "sh",
                    langConfig.getDockerImage(),
                    "/" + langConfig.getScriptFile(), String.valueOf(timeLimit), memoryLimit
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

            log.info("========== 判题完成 ==========");
            return resp;
        } catch (JudgeException e) {
            log.error("判题异常: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("判题未知异常", e);
            throw new JudgeException("判题系统异常: " + e.getMessage(), e);
        } finally {
            if (!judgeConfig.isKeepTempFiles()) {
                try {
                    log.info("清理临时目录: {}", taskRoot);
                    FileOperationUtil.deleteDir(taskRoot);
                    log.info("临时目录已清理");
                } catch (Exception e) {
                    log.error("清理临时目录失败: {}", taskRoot, e);
                }
            }
        }
    }

    private void validateAndNormalizeCode(JudgeRequest request) {
        String code = request.getCode();

        if (code == null || code.trim().isEmpty()) {
            throw new JudgeException("代码不能为空");
        }

        String language = request.getLanguage().toLowerCase();

        switch (language) {
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
                throw new JudgeException("不支持的语言: " + language);
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

    @Override
    public boolean checkDockerStatus() {
        try {
            log.info("检查 Docker 状态...");
            CommandExecutor.CommandResult result = CommandExecutor.execute(new String[]{"docker", "info"}, 5);
            boolean isRunning = result.exitCode() == 0;
            log.info("Docker 状态: {}", isRunning ? "正常运行" : "未运行");
            return isRunning;
        } catch (Exception e) {
            log.error("Docker状态检查失败", e);
            return false;
        }
    }
}
