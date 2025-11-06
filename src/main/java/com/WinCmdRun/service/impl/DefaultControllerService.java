package com.WinCmdRun.service.impl;

import com.WinCmdRun.controller.WinCmdRunController;
import com.WinCmdRun.model.Config;
import com.WinCmdRun.model.Program;
import com.WinCmdRun.service.ControllerService;
import com.WinCmdRun.util.ConfigUtil;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class DefaultControllerService implements ControllerService {

    private final WinCmdRunController controller;
    private Config config;



    // 修正构造方法的语法错误
    public DefaultControllerService(WinCmdRunController controller) {
        this.controller = controller;
    }

    @Override
    public void initialize() {
        // 初始化控件
        initControls();
        // 加载配置（不存在则创建默认配置）
        loadOrCreateConfig();
        // 刷新列表数据
        refreshGroupList();
        refreshProgramList();
    }

    /**
     * 初始化所有UI控件，确保不为null
     */
    private void initControls() {
        // 程序分组列表
        if (controller.groupListView == null) {
            controller.groupListView = new ListView<>();
            controller.groupListView.setPrefWidth(150.0);
            controller.groupListView.setPrefHeight(550.0);
        }

        // 程序列表
        if (controller.programListView == null) {
            controller.programListView = new ListView<>();
            controller.programListView.setPrefWidth(200.0);
            controller.programListView.setPrefHeight(550.0);
        }

        // 程序信息展示控件
        if (controller.programNameLabel == null) {
            controller.programNameLabel = new Label();
        }
        if (controller.localPathField == null) {
            controller.localPathField = new TextField();
        }
        if (controller.updatePathField == null) {
            controller.updatePathField = new TextField();
        }
        if (controller.remarksArea == null) {
            controller.remarksArea = new TextArea();
            controller.remarksArea.setPrefHeight(60.0);
        }
        if (controller.targetArea == null) {  // 目标控件初始化
            controller.targetArea = new TextArea();
            controller.targetArea.setPrefHeight(60.0);
        }

        // 启动参数控件
        if (controller.paramsField1 == null) controller.paramsField1 = new TextField();
        if (controller.paramsField2 == null) controller.paramsField2 = new TextField();
        if (controller.paramsField3 == null) controller.paramsField3 = new TextField();
    }

    /**
     * 加载配置文件，若不存在则创建默认配置
     */
    private void loadOrCreateConfig() {
        config = ConfigUtil.loadConfig();
        if (config == null) {
            config = new Config();
            // 添加默认分组
            config.getGroups().add("默认分组");
            ConfigUtil.saveConfig(config);
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void refreshGroupList() {
        // 清空现有数据
        controller.groupListView.getItems().clear();

        // 加载分组数据（若无数据则显示提示）
        if (config != null && config.getGroups() != null && !config.getGroups().isEmpty()) {
            controller.groupListView.getItems().addAll(config.getGroups());
        } else {
            controller.groupListView.getItems().add("(右键添加分组)");
        }
    }

    @Override
    public void refreshProgramList() {
        // 清空现有数据
        controller.programListView.getItems().clear();

        // 获取选中的分组
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        // 加载对应分组的程序（若无数据则显示提示）
        if (selectedGroup != null && !selectedGroup.equals("(右键添加分组)")
                && config != null && config.getPrograms() != null) {
            for (Program program : config.getPrograms()) {
                if (program != null && selectedGroup.equals(program.getGroup())) {
                    controller.programListView.getItems().add(program.getName());
                }
            }
            // 若当前分组无程序，显示提示
            if (controller.programListView.getItems().isEmpty()) {
                controller.programListView.getItems().add("(右键添加程序)");
            }
        } else {
            controller.programListView.getItems().add("(请先选择分组)");
        }
    }

    /**
     * 加载选中程序的配置到表单
     * @param programName 程序名称
     */
    public void loadProgramConfig(String programName) {
        // 清空表单的情况：程序名称无效或未选择分组
        if (programName == null || programName.isEmpty()
                || programName.equals("(右键添加程序)")) {
            clearProgramForm();
            return;
        }

        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null || selectedGroup.isEmpty()
                || selectedGroup.equals("(右键添加分组)")) {
            clearProgramForm();
            return;
        }

        // 查找并加载程序配置
        Program targetProgram = findProgram(programName, selectedGroup);
        if (targetProgram != null) {
            controller.programNameLabel.setText(targetProgram.getName());
            controller.localPathField.setText(targetProgram.getLocalPath() != null ? targetProgram.getLocalPath() : "");
            controller.updatePathField.setText(targetProgram.getUpdatePath() != null ? targetProgram.getUpdatePath() : "");
            controller.targetArea.setText(targetProgram.getTarget() != null ? targetProgram.getTarget() : "");  // 加载目标内容
            controller.remarksArea.setText(targetProgram.getRemarks() != null ? targetProgram.getRemarks() : "");
            controller.paramsField1.setText(targetProgram.getStartupParams1() != null ? targetProgram.getStartupParams1() : "");
            controller.paramsField2.setText(targetProgram.getStartupParams2() != null ? targetProgram.getStartupParams2() : "");
            controller.paramsField3.setText(targetProgram.getStartupParams3() != null ? targetProgram.getStartupParams3() : "");
        } else {
            clearProgramForm();
        }
    }

    /**
     * 清空表单数据
     */
    public void clearProgramForm() {
        controller.programNameLabel.setText("");
        controller.localPathField.clear();
        controller.updatePathField.clear();
        controller.targetArea.clear();
        controller.remarksArea.clear();
        controller.paramsField1.clear();
        controller.paramsField2.clear();
        controller.paramsField3.clear();
        controller.param1Radio.setSelected(true);  // 默认选中参数1
    }

    /**
     * 根据程序名称和分组查找程序
     * @param programName 程序名称
     * @param group 所属分组
     * @return 找到的程序对象（未找到返回null）
     */
    private Program findProgram(String programName, String group) {
        if (config == null || config.getPrograms() == null) {
            return null;
        }
        for (Program program : config.getPrograms()) {
            if (program != null
                    && program.getName().equals(programName)
                    && program.getGroup().equals(group)) {
                return program;
            }
        }
        return null;
    }


}