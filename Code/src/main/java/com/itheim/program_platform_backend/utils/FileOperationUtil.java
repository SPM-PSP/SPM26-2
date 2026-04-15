package com.itheim.program_platform_backend.utils;

import com.itheim.program_platform_backend.exception.JudgeException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

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
        try {
            FileUtils.copyFile(new File(source), new File(target));
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

    // Windows路径转Docker可识别格式
    public static String toDockerPath(String localPath) {
        return localPath.replace("\\", "/");
    }
}