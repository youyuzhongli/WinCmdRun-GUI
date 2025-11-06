package com.WinCmdRun.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.WinCmdRun.model.Group;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.json";
    private static final Gson gson = new Gson();

    // 确保配置目录存在
    private static void ensureConfigDir() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 保存分组列表到JSON
    public static void saveGroups(List<Group> groups) {
        ensureConfigDir();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(groups, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从JSON加载分组列表
    public static List<Group> loadGroups() {
        ensureConfigDir();
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // 返回空列表如果文件不存在
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<Group>>(){}.getType();
            List<Group> groups = gson.fromJson(reader, type);
            return groups != null ? groups : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}