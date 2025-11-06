package com.WinCmdRun.service.impl;

import com.WinCmdRun.controller.WinCmdRunController;
import com.WinCmdRun.model.Program;
import com.WinCmdRun.service.MenuService;
import com.WinCmdRun.util.ConfigUtil;
import com.WinCmdRun.util.DialogUtil;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuServiceImpl implements MenuService {
    private final WinCmdRunController controller;

    public MenuServiceImpl(WinCmdRunController controller) {
        this.controller = controller;
    }

    @Override
    public void initContextMenus() {
        initGroupContextMenu();
        initProgramContextMenu();
    }

    // 初始化分组右键菜单
    private void initGroupContextMenu() {
        ContextMenu groupMenu = new ContextMenu();

        MenuItem addGroupItem = new MenuItem("新增分组");
        addGroupItem.setOnAction(e -> addGroup());

        MenuItem deleteGroupItem = new MenuItem("删除分组");
        deleteGroupItem.setOnAction(e -> deleteGroup());

        MenuItem renameGroupItem = new MenuItem("重命名分组");
        renameGroupItem.setOnAction(e -> renameGroup());

        groupMenu.getItems().addAll(addGroupItem, deleteGroupItem, renameGroupItem);
        controller.groupListView.setContextMenu(groupMenu);

        // 点击空白处关闭菜单
        controller.groupListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                groupMenu.hide();
            }
        });
    }

    // 初始化程序右键菜单
    private void initProgramContextMenu() {
        ContextMenu programMenu = new ContextMenu();

        MenuItem addProgramItem = new MenuItem("新增程序");
        addProgramItem.setOnAction(e -> addProgram());

        MenuItem deleteProgramItem = new MenuItem("删除程序");
        deleteProgramItem.setOnAction(e -> deleteProgram());

        MenuItem renameProgramItem = new MenuItem("重命名程序");
        renameProgramItem.setOnAction(e -> renameProgram());

        // 新增"移动到分组"菜单项
        MenuItem moveProgramItem = new MenuItem("移动到分组");
        moveProgramItem.setOnAction(e -> moveProgram());

        programMenu.getItems().addAll(addProgramItem, deleteProgramItem, renameProgramItem, moveProgramItem);
        controller.programListView.setContextMenu(programMenu);

        // 点击空白处关闭菜单
        controller.programListView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                programMenu.hide();
            }
        });
    }


    // 移动程序到其他分组的实现方法
    private void moveProgram() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String currentGroup = controller.groupListView.getSelectionModel().getSelectedItem();

        // 检查选中状态
        if (selectedProgram == null || currentGroup == null ||
                selectedProgram.equals("(右键添加程序)") ||
                currentGroup.equals("(右键添加分组)")) {
            DialogUtil.showAlert("提示", "请先选择有效的程序和当前分组");
            return;
        }

        // 获取可选的目标分组（排除当前分组）
        List<String> targetGroups = controller.getConfig().getGroups().stream()
                .filter(group -> !group.equals(currentGroup))
                .collect(Collectors.toList());

        if (targetGroups.isEmpty()) {
            DialogUtil.showAlert("提示", "没有其他可用分组，请先创建新分组");
            return;
        }

        // 显示分组选择对话框
        ChoiceDialog<String> dialog = new ChoiceDialog<>(targetGroups.get(0), targetGroups);
        dialog.setTitle("移动程序");
        dialog.setHeaderText("选择目标分组");
        dialog.setContentText("请选择要移动到的分组:");

        // 处理选择结果
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetGroup -> {
            // 查找并移动程序
            for (Program p : controller.getConfig().getPrograms()) {
                if (p.getName().equals(selectedProgram) && p.getGroup().equals(currentGroup)) {
                    p.setGroup(targetGroup);
                    break;
                }
            }

            // 保存配置并刷新列表
            ConfigUtil.saveConfig(controller.getConfig());
            controller.refreshProgramList();
        });
    }

    // 新增分组
    private void addGroup() {
        Optional<String> result = DialogUtil.showInputDialog("新增分组", "请输入分组名称：");
        result.ifPresent(groupName -> {
            groupName = groupName.trim();
            if (!groupName.isEmpty() && !controller.getConfig().getGroups().contains(groupName)) {
                controller.getConfig().getGroups().add(groupName);
                ConfigUtil.saveConfig(controller.getConfig());
                controller.refreshGroupList();
            }
        });
    }

    // 删除分组
    private void deleteGroup() {
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            DialogUtil.showAlert("提示", "请先选择一个分组");
            return;
        }

        controller.getConfig().getGroups().remove(selectedGroup);

        Iterator<Program> iterator = controller.getConfig().getPrograms().iterator();
        while (iterator.hasNext()) {
            Program p = iterator.next();
            if (p != null && p.getGroup().equals(selectedGroup)) {
                iterator.remove();
            }
        }

        ConfigUtil.saveConfig(controller.getConfig());
        controller.refreshGroupList();
        controller.refreshProgramList();
    }

    // 重命名分组
    private void renameGroup() {
        String oldGroupName = controller.groupListView.getSelectionModel().getSelectedItem();
        if (oldGroupName == null) {
            DialogUtil.showAlert("提示", "请先选择一个分组");
            return;
        }

        Optional<String> result = DialogUtil.showInputDialog("重命名分组", "请输入新名称：", oldGroupName);
        result.ifPresent(newGroupName -> {
            newGroupName = newGroupName.trim();
            if (newGroupName.isEmpty() || newGroupName.equals(oldGroupName)) return;
            if (controller.getConfig().getGroups().contains(newGroupName)) {
                DialogUtil.showAlert("提示", "分组名称已存在");
                return;
            }

            int index = controller.getConfig().getGroups().indexOf(oldGroupName);
            controller.getConfig().getGroups().set(index, newGroupName);

            for (Program p : controller.getConfig().getPrograms()) {
                if (p.getGroup().equals(oldGroupName)) {
                    p.setGroup(newGroupName);
                }
            }

            ConfigUtil.saveConfig(controller.getConfig());
            controller.refreshGroupList();
            controller.refreshProgramList();
        });
    }

    // 新增程序（初始化3个参数）
    private void addProgram() {
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            DialogUtil.showAlert("提示", "请先选择一个分组");
            return;
        }

        Optional<String> result = DialogUtil.showInputDialog("新增程序", "请输入程序名称：");
        result.ifPresent(programName -> {
            programName = programName.trim();
            if (programName.isEmpty()) {
                DialogUtil.showAlert("提示", "程序名称不能为空");
                return;
            }

            if (isProgramDuplicate(programName, selectedGroup)) {
                DialogUtil.showAlert("提示", "该分组下已存在同名程序");
                return;
            }

            Program newProgram = new Program(
                    programName,       // 1. 名称
                    "",                // 2. localPath
                    "",                // 3. updatePath
                    "",                // 4. remarks
                    "",                // 5. target（新增的目标字段）
                    selectedGroup,     // 6. group
                    "",                // 7. startupParams1
                    "",                // 8. startupParams2
                    ""                 // 9. startupParams3
            );
            controller.getConfig().getPrograms().add(newProgram);
            ConfigUtil.saveConfig(controller.getConfig());
            controller.refreshProgramList();
        });
    }

    // 删除程序
    private void deleteProgram() {
        String selectedProgram = controller.programListView.getSelectionModel().getSelectedItem();
        String selectedGroup = controller.groupListView.getSelectionModel().getSelectedItem();
        if (selectedProgram == null || selectedGroup == null) {
            DialogUtil.showAlert("提示", "请先选择程序和分组");
            return;
        }

        Iterator<Program> iterator = controller.getConfig().getPrograms().iterator();
        while (iterator.hasNext()) {
            Program p = iterator.next();
            if (p != null && p.getName().equals(selectedProgram) && p.getGroup().equals(selectedGroup)) {
                iterator.remove();
            }
        }

        ConfigUtil.saveConfig(controller.getConfig());
        controller.refreshProgramList();
        ((DefaultControllerService) controller.getControllerService()).clearProgramForm();
    }

    // 重命名程序
    private void renameProgram() {
        String oldProgramName = controller.programListView.getSelectionModel().getSelectedItem();
        String groupName = controller.groupListView.getSelectionModel().getSelectedItem();
        if (oldProgramName == null || groupName == null) {
            DialogUtil.showAlert("提示", "请先选择一个程序");
            return;
        }

        Optional<String> result = DialogUtil.showInputDialog("重命名程序", "请输入新名称：", oldProgramName);
        result.ifPresent(newProgramName -> {
            newProgramName = newProgramName.trim();
            if (newProgramName.isEmpty() || newProgramName.equals(oldProgramName)) return;

            if (isProgramDuplicate(newProgramName, groupName)) {
                DialogUtil.showAlert("提示", "该分组下已存在同名程序");
                return;
            }

            for (Program p : controller.getConfig().getPrograms()) {
                if (p.getName().equals(oldProgramName) && p.getGroup().equals(groupName)) {
                    p.setName(newProgramName);
                }
            }

            ConfigUtil.saveConfig(controller.getConfig());
            controller.refreshProgramList();
        });
    }

    // 检查程序是否重名
    private boolean isProgramDuplicate(String programName, String group) {
        for (Program p : controller.getConfig().getPrograms()) {
            if (p.getName().equals(programName) && p.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }
}