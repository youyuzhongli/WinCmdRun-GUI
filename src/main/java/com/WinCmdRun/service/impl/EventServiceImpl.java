package com.WinCmdRun.service.impl;

import com.WinCmdRun.controller.WinCmdRunController;
import com.WinCmdRun.model.Program;
import com.WinCmdRun.service.EventService;
import com.WinCmdRun.util.ConfigUtil;
import com.WinCmdRun.util.DialogUtil;
import com.WinCmdRun.util.FileUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class EventServiceImpl implements EventService {
    private final WinCmdRunController controller;
    private final DefaultControllerService controllerService;

    public EventServiceImpl(WinCmdRunController controller) {
        this.controller = controller;
        this.controllerService = (DefaultControllerService) controller.getControllerService();
    }

    @Override
    public void bindEvents() {
        bindGroupSelectionEvent();
        bindProgramSelectionEvent();
        bindButtonEvents();
        bindBrowseUpdateBtnEvent();
    }

    /**
     * 绑定分组选择事件
     */
    private void bindGroupSelectionEvent() {
        controller.groupListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    controller.refreshProgramList();
                    controllerService.clearProgramForm();
                }
        );
    }

    /**
     * 绑定程序选择事件
     */
    private void bindProgramSelectionEvent() {
        controller.programListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> controllerService.loadProgramConfig(newVal)
        );
    }

    /**
     * 绑定所有按钮事件
     */
    private void bindButtonEvents() {
        // 浏览本地路径
        controller.browseLocalBtn.setOnAction(e -> browsePath(controller.localPathField));
        // 浏览远程链接
//        controller.browseUpdateBtn.setOnAction(e -> browsePath(controller.updatePathField));
        controller.browseUpdateBtn.setOnAction(e -> browsePath(controller.updatePathField));
        // 启动程序
        controller.startBtn.setOnAction(e -> startProgram());
        // 保存配置
        controller.saveBtn.setOnAction(e -> saveProgramConfig());
        // 生成CMD脚本
//        controller.genCmdBtn.setOnAction(e -> generateCmdScript());
        //使用说明
        controller.helpBtn.setOnAction(e -> handleShowHelp());
        // 打开文件夹
        controller.openFolderBtn.setOnAction(e -> openProgramFolder());
    }

    // ------------------------------ 新增：使用说明按钮事件 ------------------------------
    @FXML
    public void handleShowHelp() {
        // 使用文本区域展示使用说明
        TextArea helpArea = new TextArea();
        helpArea.setEditable(false);
        helpArea.setPrefWidth(600);
        helpArea.setPrefHeight(400);
        helpArea.setText(getHelpContent());  // 加载说明内容

        // 创建对话框
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("使用说明");
        dialog.setHeaderText("LinkTools 工具使用指南");
        dialog.getDialogPane().setContent(helpArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);  // 确定按钮

        // 显示对话框
        dialog.showAndWait();
    }

    // 定义使用说明内容
// 在 WinCmdRunController 中修改 getHelpContent() 方法
    private String getHelpContent() {
        return "LinkTools 使用说明\n" +
                "=====================\n" +
                "一、分组管理\n" +
                "  1. 左侧为分组列表，右键点击可：\n" +
                "     - 添加分组：创建新的程序分组\n" +
                "     - 删除分组：删除当前选中的分组（需先选择分组）\n" +
                "  2. 点击分组名称可切换到该分组，右侧会显示该分组下的程序列表\n\n" +

                "二、程序管理\n" +
                "  1. 右侧为程序列表，右键点击可：\n" +
                "     - 添加程序：在当前分组下创建新程序\n" +
                "     - 编辑程序：修改选中程序的配置（需先选择程序）\n" +
                "     - 删除程序：移除选中的程序\n" +
                "     - 移动到分组：将程序迁移到其他分组\n" +
                "  2. 选中程序后，下方表单会显示该程序的详细配置，修改后点击「保存配置」生效\n\n" +

                "三、参数配置\n" +
                "  1. 支持3组参数（Param1/Param2/Param3），可通过单选按钮切换默认使用的参数组\n" +
                "  2. 参数中支持以下变量，执行时会自动替换为实际值：\n" +
                "     - [file]：程序文件的绝对路径（如：C:\\tools\\app.exe）\n" +
                "     - [ip]：目标文本中的IP地址（需目标格式为「ip:port」，如目标为192.168.1.1:8080时，[ip]会替换为192.168.1.1）\n" +
                "     - [port]：目标文本中的端口号（同上例，[port]会替换为8080）\n" +
                "     - [log]：特殊标记，添加此参数后，程序执行结果会自动追加到根目录的log.txt文件中（日志包含执行时间、命令和输出）\n\n" +

                "四、目标配置\n" +
                "  1. 「目标」文本框支持输入多行内容，每行代表一个执行目标\n" +
                "  2. 执行程序时，会按顺序对每行目标执行一次程序（配合[ip]/[port]变量使用）\n" +
                "  3. 若目标为空，程序会自动执行一次默认命令（仅使用[file]变量）\n\n" +

                "五、功能按钮说明\n" +
                "  1. 浏览本地路径：选择程序的本地可执行文件（.exe、.bat等）\n" +
                "  2. 浏览更新路径：选择程序的更新包文件（可选）\n" +
                "  3. 启动程序：按当前配置执行程序（根据目标行数批量执行）\n" +
                "  4. 保存配置：保存当前程序的所有参数设置\n" +
                "  5. 打开文件夹：打开「本地路径」所指向文件的所在目录\n" +
                "  6. 使用说明：显示本帮助文档\n\n" +

                "六、日志说明\n" +
                "  - 当参数中包含[log]时，执行日志会保存到程序根目录的log.txt\n" +
                "  - 日志内容包括：执行时间、执行命令、程序输出结果、错误信息（如有）\n" +
                "  - 日志采用追加模式，不会覆盖历史记录，可手动删除log.txt清空日志\n";
    }


    /**
     * 浏览文件路径并填充到输入框
     */
    private void browsePath(TextField textField) {
        if (textField == null) return;
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(textField.getScene().getWindow());
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * 绑定远程链接浏览按钮事件：点击后用浏览器打开链接
     */
    private void bindBrowseUpdateBtnEvent() {
        controller.browseUpdateBtn.setOnAction(event -> {
            // 1. 获取输入框中的远程链接
            String remoteUrl = controller.updatePathField.getText();

            // 2. 调用工具类打开浏览器访问链接
            openRemoteLink(remoteUrl);
        });
    }


    /**
     * 启动程序（仅传递本地路径、选中参数、目标文本）
     */
    private void startProgram() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        if (selectedProgram == null || selectedGroup == null
                || selectedProgram.equals("(右键添加程序)")
                || selectedGroup.equals("(右键添加分组)")) {
            DialogUtil.showAlert("提示", "请先选择有效程序和分组");
            return;
        }

        Program program = findProgram(selectedProgram, selectedGroup);
        if (program == null || program.getLocalPath() == null || program.getLocalPath().trim().isEmpty()) {
            DialogUtil.showAlert("错误", "程序本地路径不存在或为空");
            return;
        }

        try {
            String selectedParams = getSelectedParams(); // 选中的参数1/2/3
            String targetText = controller.targetArea.getText().trim(); // 目标文本（解析ip/port）

            // 启动程序
            FileUtil.startProgramInLocalDir(program.getLocalPath(), selectedParams, targetText);
        } catch (FileNotFoundException e) {
            DialogUtil.showAlert("错误", "文件不存在: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            DialogUtil.showAlert("错误", e.getMessage());
        } catch (Exception e) {
            DialogUtil.showAlert("错误", "启动失败: " + e.getMessage());
        }
    }


    /**
     * 生成CMD脚本（同步调整）
     */
    private void generateCmdScript() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        if (selectedProgram == null || selectedGroup == null
                || selectedProgram.equals("(右键添加程序)")
                || selectedGroup.equals("(右键添加分组)")) {
            DialogUtil.showAlert("提示", "请先选择有效程序和分组");
            return;
        }

        Program program = findProgram(selectedProgram, selectedGroup);
        if (program == null || program.getLocalPath() == null || program.getLocalPath().trim().isEmpty()) {
            DialogUtil.showAlert("错误", "程序本地路径不存在或为空");
            return;
        }

        try {
            String selectedParams = getSelectedParams();
            String path = FileUtil.generateCmdScriptInLocalDir(program, selectedParams);
            DialogUtil.showAlert("成功", "CMD脚本已生成：\n" + path);
        } catch (Exception e) {
            DialogUtil.showAlert("错误", "生成脚本失败：" + e.getMessage());
        }
    }

    /**
     * 保存程序配置（包含目标和备注）
     */
    private void saveProgramConfig() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        // 前置校验
        if (selectedProgram == null || selectedGroup == null
                || selectedProgram.equals("(右键添加程序)")
                || selectedGroup.equals("(右键添加分组)")) {
            DialogUtil.showAlert("提示", "请先选择有效程序和分组");
            return;
        }

        for (Program p : controller.getConfig().getPrograms()) {
            if (p != null && p.getName().equals(selectedProgram) && p.getGroup().equals(selectedGroup)) {
                p.setLocalPath(controller.localPathField.getText().trim());
                p.setUpdatePath(controller.updatePathField.getText().trim());
                p.setRemarks(controller.remarksArea.getText().trim());
                p.setTarget(controller.targetArea.getText().trim());  // 保存目标内容
                p.setStartupParams1(controller.paramsField1.getText().trim());
                p.setStartupParams2(controller.paramsField2.getText().trim());
                p.setStartupParams3(controller.paramsField3.getText().trim());
                break;
            }
        }

        ConfigUtil.saveConfig(controller.getConfig());
        DialogUtil.showAlert("成功", "配置已保存");
    }

//    /**
//     * 生成CMD脚本
//     */
//    private void generateCmdScript() {
//        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
//        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();
//
//        if (selectedProgram == null || selectedGroup == null
//                || selectedProgram.equals("(右键添加程序)")
//                || selectedGroup.equals("(右键添加分组)")) {
//            DialogUtil.showAlert("提示", "请先选择有效程序和分组");
//            return;
//        }
//
//        Program program = findProgram(selectedProgram, selectedGroup);
//        if (program == null || program.getLocalPath() == null || program.getLocalPath().trim().isEmpty()) {
//            DialogUtil.showAlert("错误", "程序本地路径不存在或为空");
//            return;
//        }
//
//        try {
//            String selectedParams = getSelectedParams();
//            String path = FileUtil.generateCmdScriptInLocalDir(program, selectedParams);
//            DialogUtil.showAlert("成功", "CMD脚本已生成：\n" + path);
//        } catch (Exception e) {
//            DialogUtil.showAlert("错误", "生成脚本失败：" + e.getMessage());
//        }
//    }

    /**
     * 获取选中的启动参数
     */
    private String getSelectedParams() {
        if (controller.param2Radio.isSelected()) {
            return controller.paramsField2.getText().trim();
        } else if (controller.param3Radio.isSelected()) {
            return controller.paramsField3.getText().trim();
        } else {
            return controller.paramsField1.getText().trim();
        }
    }

    /**
     * 打开程序所在文件夹
     */
    private void openProgramFolder() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        if (selectedProgram == null || selectedGroup == null
                || selectedProgram.equals("(右键添加程序)")
                || selectedGroup.equals("(右键添加分组)")) {
            DialogUtil.showAlert("提示", "请先选择有效程序和分组");
            return;
        }

        Program program = findProgram(selectedProgram, selectedGroup);
        if (program == null || program.getLocalPath() == null || program.getLocalPath().trim().isEmpty()) {
            DialogUtil.showAlert("错误", "程序本地路径不存在或为空");
            return;
        }

        try {
            FileUtil.openFolder(program.getLocalPath());
        } catch (Exception e) {
            DialogUtil.showAlert("错误", "打开文件夹失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称和分组查找程序
     */
    private Program findProgram(String programName, String group) {
        if (controller.getConfig() == null || controller.getConfig().getPrograms() == null) {
            return null;
        }
        for (Program p : controller.getConfig().getPrograms()) {
            if (p != null && p.getName().equals(programName) && p.getGroup().equals(group)) {
                return p;
            }
        }
        return null;
    }


    /**
     * 打开远程链接（通过系统默认浏览器）
     *
     * @param remoteUrl 远程链接URL
     */
    public static void openRemoteLink(String remoteUrl) {
        // 检查URL是否为空
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "远程链接为空",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String url = remoteUrl.trim();
        // 简单处理URL格式，确保以http/https开头
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        try {
            // 检查系统是否支持Desktop功能
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                // 检查是否支持浏览操作
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    // 打开默认浏览器访问URL
                    desktop.browse(new URI(url));
                } else {
                    JOptionPane.showMessageDialog(null,
                            "系统不支持浏览器操作",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "系统不支持桌面操作",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                    "链接格式不正确: " + url,
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "无法打开链接: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }

    }
}