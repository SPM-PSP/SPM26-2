package com.itheim.program_platform_backend.utils;

import com.itheim.program_platform_backend.exception.JudgeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileOperationUtil {

    public static void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new JudgeException("创建目录失败: " + path);
        }
    }

    public static void writeFile(String path, String content) {
        try {
            FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new JudgeException("写入文件失败: " + path, e);
        }
    }

    public static void copyFile(String source, String target) {
        File sourceFile = new File(source);

        if (!sourceFile.exists()) {
            String absolutePath = sourceFile.getAbsolutePath();
            log.error("源文件不存在 - 相对路径: {}, 绝对路径: {}", source, absolutePath);
            throw new JudgeException("源文件不存在: " + source + " (绝对路径: " + absolutePath + ")");
        }

        if (!sourceFile.isFile()) {
            throw new JudgeException("源路径不是文件: " + source);
        }

        try {
            FileUtils.copyFile(sourceFile, new File(target));
            log.debug("文件复制成功: {} -> {}", source, target);
        } catch (Exception e) {
            throw new JudgeException("复制文件失败: " + source + " -> " + target, e);
        }
    }

    public static String readFile(String path) {
        File file = new File(path);
        if (!file.exists()) return "";
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new JudgeException("读取文件失败: " + path, e);
        }
    }

    public static void deleteDir(String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (Exception e) {
            throw new JudgeException("删除目录失败: " + path, e);
        }
    }

    public static String toDockerPath(String localPath) {
        return localPath.replace("\\", "/");
    }
}
