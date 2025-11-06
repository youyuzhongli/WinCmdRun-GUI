package com.WinCmdRun.util;

import com.WinCmdRun.model.Program;
import java.awt.Desktop;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileUtil {

    // 维护一个单线程池用于异步执行无参数命令
    private static final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    /**
     * 启动程序（支持[log]参数保存执行结果）
     */
    public static void startProgramInLocalDir(String filePath, String selectedParams, String targetText) throws Exception {
        File programFile = new File(filePath);
        if (!programFile.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }
        if (!programFile.isFile()) {
            throw new IllegalArgumentException("不是有效的文件: " + filePath);
        }

        // 1. 处理参数（标记是否需要日志）
        boolean isParamsEmpty = (selectedParams == null || selectedParams.trim().isEmpty());
        String rawParams = isParamsEmpty ? "" : selectedParams.trim();
        boolean needLog = rawParams.contains("[log]"); // 检测是否包含[log]参数

        // 2. 分割目标文本为多行（忽略空行）
        List<String> targetLines = new ArrayList<>();
        if (targetText != null && !targetText.trim().isEmpty()) {
            String[] lines = targetText.split("\\r?\\n");
//            for (String line : lines) {
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    targetLines.add(trimmedLine);
                }
            }
        } else {
            targetLines.add("[DEFAULT_EXECUTE]"); // 目标为空时执行默认程序
        }

        // 3. 构建批量执行脚本（支持日志输出）
        File batFile = createBatchExecutionBatScript(
                programFile,
                rawParams,
                targetLines,
                isParamsEmpty,
                needLog // 传递日志标记
        );

        // 4. 启动CMD窗口执行脚本：无参数时异步执行且不显示窗口，有参数时正常显示
        if (isParamsEmpty) {
            // 异步执行无参数命令，不显示CMD窗口
            asyncExecutor.submit(() -> {

                try {
                    // 获取programFile.getAbsolutePath()的完整路径（实际为本地文件路径）
                    String fullExePath = programFile.getAbsolutePath();
                    File exeFile = new File(fullExePath);

                    // 1. 提取目标目录（exe文件所在目录的父目录）
                    // 示例1：E:\tools\TscanPlus\TscanPlus_Win_Amd64.exe → 父目录为E:\tools\TscanPlus → 上一级为E:\tools\TscanPlus（因该目录已是二级目录）
                    // 示例2：E:\tools\api-explorer\API-Explorer_v2.1.0\API-Explorer.exe → 父目录为API-Explorer_v2.1.0 → 上一级为E:\tools\api-explorer
                    File exeParentDir = exeFile.getParentFile(); // 获取exe所在的直接目录
                    String targetDir = exeParentDir.getAbsolutePath();

                    // 2. 提取exe文件名（不含路径）
                    String exePath = exeFile.getName(); // 示例1：TscanPlus_Win_Amd64.exe；示例2：API-Explorer.exe

                    // 构建CMD命令：先切换到targetDir，再执行exePath
                    String cmd = String.format(
//                            "cmd.exe /c start \"程序启动控制台\" cmd /k \"cd /d \"%s\" && \"%s\"\"",
//                            "cmd.exe /c start \"程序启动控制台\" cmd /c \"cd /d \"%s\" && \"%s\" && ping 127.0.0.1 -n 1 > nul\"",
                            "cmd.exe /c start \"程序启动控制台\" cmd /c \"cd /d \"%s\" && start \"\" \"%s\" && ping 127.0.0.1 -n 3 > nul\"",
                            targetDir,  // 切换到的目标目录（E:\tools\TscanPlus 或 E:\tools\api-explorer）
                            exePath     // 执行的exe文件名（TscanPlus_Win_Amd64.exe 或 API-Explorer.exe）
                    );

                    Runtime.getRuntime().exec(cmd).waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            // 有参数时正常显示CMD窗口
            Runtime.getRuntime().exec(new String[]{
                    "cmd.exe", "/c", "start \"cmd\" \"" + batFile.getAbsolutePath() + "\""
            });
        }
    }

    /**
     * 创建批量执行脚本（增加日志输出逻辑）
     */
    private static File createBatchExecutionBatScript(File programFile, String rawParams,
                                                      List<String> targetLines, boolean isParamsEmpty,
                                                      boolean needLog) throws IOException {
        // 1. 确保tmp文件夹存在（程序根目录下）
        File rootDir = new File(System.getProperty("user.dir"));
        File tmpDir = new File(rootDir, "tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        // 2. 日志文件路径（程序根目录下的log.txt）
        File logFile = new File(rootDir, "log.txt");

        // 3. 生成临时脚本文件
        String fileName = "linktools_batch_launcher_" + System.currentTimeMillis() + ".bat";
        File batFile = new File(tmpDir, fileName);
        batFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(batFile), "GBK"))) {
            writer.write("@echo off");
            writer.newLine();
            writer.write("chcp 936 > nul"); // 解决中文乱码
            writer.newLine();
            writer.write("color 0A");
            writer.newLine();

            // 4. 标题显示（包含日志状态）
            String logStatus = needLog ? "（带日志输出）" : "";
            String title = targetLines.size() == 1 && targetLines.get(0).equals("[DEFAULT_EXECUTE]")
                    ? "默认执行控制台" + logStatus
                    : "批量执行控制台（共" + targetLines.size() + "条命令）" + logStatus;
            writer.write("title " + title);
            writer.newLine();
            writer.write("echo ===============================================");
            writer.newLine();
            writer.write("echo 临时文件目录: " + tmpDir.getAbsolutePath());
            writer.newLine();
            writer.write("echo 程序路径: " + programFile.getAbsolutePath());
            writer.newLine();
            writer.write("echo 启动参数: " + (rawParams.isEmpty() ? "无" : rawParams));
            writer.newLine();
            // 显示日志状态
            writer.write("echo 日志输出: " + (needLog ? "已启用（保存到 " + logFile.getAbsolutePath() + "）" : "未启用"));
            writer.newLine();
            writer.write("echo 执行类型: " + (targetLines.size() == 1 && targetLines.get(0).equals("[DEFAULT_EXECUTE]")
                    ? "目标为空，执行默认程序"
                    : "目标行数: " + targetLines.size()));
            writer.newLine();
            writer.write("echo ===============================================");
            writer.newLine();

            // 5. 处理目标行执行逻辑（增加日志输出）
            String filePath = programFile.getAbsolutePath();
            int lineNum = 1;

            for (String line : targetLines) {
                boolean isDefault = line.equals("[DEFAULT_EXECUTE]");

                Map<String, String> variables = new HashMap<>();
                variables.put("file", filePath);
                if (!isDefault) {
                    parseTargetVariables(line, variables);
                }

                // 替换参数中的[log]（实际执行时不需要显示该标记）
                String processedParams = replaceVariables(rawParams, variables).replace("[log]", "");
                String fullCommand = buildCommandWithoutRedundancy(filePath, processedParams);

                // 6. 若需要日志，添加输出重定向（>> 追加到日志文件）
                if (needLog) {
                    // 记录当前命令到日志
                    writer.write("echo =============================================== >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行时间: %date% %time% >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行命令: " + fullCommand + " >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行结果: >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    // 执行命令并将输出追加到日志
                    fullCommand += " >> \"" + logFile.getAbsolutePath() + "\" 2>&1";
                }

                // 输出执行信息
                if (isDefault) {
                    writer.write("echo 执行默认程序...");
                } else {
                    writer.write("echo 开始执行第" + lineNum + "行（目标: " + line + "）...");
                }
                writer.newLine();
                writer.write("echo 命令: " + (needLog ? fullCommand.replace(">> \"" + logFile.getAbsolutePath() + "\" 2>&1", "") : fullCommand));
                writer.newLine();
                writer.write(fullCommand); // 执行命令（带日志重定向）
                writer.newLine();

                // 执行完成提示
                if (isDefault) {
                    writer.write("echo 默认程序执行完成");
                } else {
                    writer.write("echo 第" + lineNum + "行执行完成");
                }
                writer.newLine();
                writer.write("echo ===============================================");
                writer.newLine();

                lineNum++;
            }

            // 7. 执行完成处理：无参数时不等待直接退出，有参数时保持窗口
            writer.write("echo 所有命令执行完毕！");
            writer.newLine();
            if (needLog) {
                writer.write("echo 日志已保存到: " + logFile.getAbsolutePath());
                writer.newLine();
            }
            if (isParamsEmpty) {
                // 无参数时直接退出（不显示等待提示）
                writer.write("exit");
            } else {
//                writer.write("echo 按任意键关闭窗口...");
                writer.newLine();
//                writer.write("pause");
            }
        }

        return batFile;
    }

    /**
     * 构建命令（去除冗余路径）
     */
    private static String buildCommandWithoutRedundancy(String filePath, String processedParams) {
        String programPath = "\"" + filePath + "\"";
        if (processedParams.isEmpty()) {
            return programPath;
        }
        if (processedParams.contains(filePath)) {
            return processedParams;
        }
        return programPath + " " + processedParams;
    }

    /**
     * 生成CMD脚本（同步支持[log]参数）
     */
    public static String generateCmdScriptInLocalDir(Program program, String selectedParams) throws IOException {
        File programFile = new File(program.getLocalPath());
        if (!programFile.exists()) {
            throw new FileNotFoundException("程序文件不存在: " + program.getLocalPath());
        }

        // 1. 检测是否需要日志
        boolean needLog = (selectedParams != null && selectedParams.contains("[log]"));
        String rawParams = (selectedParams == null ? "" : selectedParams).replace("[log]", ""); // 移除[log]标记

        // 2. 日志文件路径
        File rootDir = new File(System.getProperty("user.dir"));
        File logFile = new File(rootDir, "log.txt");
        File tmpDir = new File(rootDir, "tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        String targetText = program.getTarget();
        List<String> targetLines = new ArrayList<>();
        if (targetText != null && !targetText.trim().isEmpty()) {
            String[] lines = targetText.split("\\r?\\n");
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    targetLines.add(trimmedLine);
                }
            }
        } else {
            targetLines.add("[DEFAULT_EXECUTE]");
        }

        // 3. 生成脚本文件
        File batFile = new File(tmpDir, program.getName() + "_批量执行.bat");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(batFile), "GBK"))) {
            writer.write("@echo off");
            writer.newLine();
            writer.write("chcp 936 > nul");
            writer.newLine();
            String logStatus = needLog ? "（带日志输出）" : "";
            String title = targetLines.size() == 1 && targetLines.get(0).equals("[DEFAULT_EXECUTE]")
                    ? program.getName() + " 默认执行脚本" + logStatus
                    : program.getName() + " 批量执行脚本" + logStatus;
            writer.write("title " + title);
            writer.newLine();
            writer.write("cd /d \"" + programFile.getParentFile().getAbsolutePath() + "\"");
            writer.newLine();

            // 4. 显示日志状态
            writer.write("echo ===============================================");
            writer.newLine();
            writer.write("echo 日志输出: " + (needLog ? "已启用（保存到 " + logFile.getAbsolutePath() + "）" : "未启用"));
            writer.newLine();
            if (targetLines.size() == 1 && targetLines.get(0).equals("[DEFAULT_EXECUTE]")) {
                writer.write("echo 目标为空，执行默认程序...");
            } else {
                writer.write("echo 共" + targetLines.size() + "条命令，按顺序执行...");
            }
            writer.newLine();
            writer.write("echo ===============================================");
            writer.newLine();

            String filePath = program.getLocalPath();
            int lineNum = 1;

            for (String line : targetLines) {
                boolean isDefault = line.equals("[DEFAULT_EXECUTE]");

                Map<String, String> variables = new HashMap<>();
                variables.put("file", filePath);
                if (!isDefault) {
                    parseTargetVariables(line, variables);
                }

                String processedParams = replaceVariables(rawParams, variables);
                String fullCommand = buildCommandWithoutRedundancy(filePath, processedParams);

                // 5. 日志输出处理
                if (needLog) {
                    writer.write("echo =============================================== >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行时间: %date% %time% >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行命令: " + fullCommand + " >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    writer.write("echo 执行结果: >> \"" + logFile.getAbsolutePath() + "\"");
                    writer.newLine();
                    fullCommand += " >> \"" + logFile.getAbsolutePath() + "\" 2>&1";
                }

                if (isDefault) {
                    writer.write("echo 执行默认程序...");
                } else {
                    writer.write("echo 第" + lineNum + "行（目标: " + line + "）:");
                }
                writer.newLine();
                writer.write("echo " + (needLog ? fullCommand.replace(">> \"" + logFile.getAbsolutePath() + "\" 2>&1", "") : fullCommand));
                writer.newLine();
                writer.write(fullCommand);
                writer.newLine();

                if (isDefault) {
                    writer.write("echo 默认程序执行完成");
                } else {
                    writer.write("echo 第" + lineNum + "行执行完成");
                }
                writer.newLine();
                writer.write("echo ===============================================");
                writer.newLine();

                lineNum++;
            }

            writer.write("echo 所有命令执行完成！");
            writer.newLine();
            if (needLog) {
                writer.write("echo 日志已保存到: " + logFile.getAbsolutePath());
                writer.newLine();
            }
            writer.write("pause");
            writer.newLine();
        }

        return batFile.getAbsolutePath();
    }

    // 其他方法保持不变
    public static void openFolder(String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.isDirectory() ? file : file.getParentFile();
        if (parentDir != null && parentDir.exists()) {
            Desktop.getDesktop().open(parentDir);
        } else {
            throw new FileNotFoundException("文件夹不存在: " + (parentDir != null ? parentDir.getAbsolutePath() : filePath));
        }
    }

    private static void parseTargetVariables(String targetText, Map<String, String> variables) {
        if (targetText == null || targetText.trim().isEmpty()) return;
        String[] parts = targetText.trim().split("@", 2);
        if (parts.length >= 1) variables.put("ip", parts[0].trim());
        if (parts.length == 2) variables.put("port", parts[1].trim());
    }

    private static String replaceVariables(String text, Map<String, String> variables) {
        String processed = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            processed = processed.replace("[" + entry.getKey() + "]", entry.getValue());
        }
        return processed;
    }
}