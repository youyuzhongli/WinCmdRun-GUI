package com.WinCmdRun.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.WinCmdRun.model.Config;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigUtil {
    private static final String CONFIG_PATH = "config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 加载配置，若文件不存在则返回默认配置（关键修复）
    public static Config loadConfig() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            // 创建默认配置文件
            saveConfig(new Config());
            return new Config();
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            System.err.println("配置文件加载失败，使用默认配置: " + e.getMessage());
            return new Config(); // 加载失败时返回空配置
        }
    }

    // 保存配置
    public static void saveConfig(Config config) {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("配置文件保存失败: " + e.getMessage());
        }
    }
}