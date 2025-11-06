package com.WinCmdRun.controller;

import com.WinCmdRun.model.Config;
import com.WinCmdRun.service.ControllerService;
import com.WinCmdRun.service.EventService;
import com.WinCmdRun.service.MenuService;
import com.WinCmdRun.service.impl.DefaultControllerService;
import com.WinCmdRun.service.impl.EventServiceImpl;
import com.WinCmdRun.service.impl.MenuServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ToggleGroup;

public class WinCmdRunController {
    // 程序分组和列表控件
    @FXML public ListView<String> groupListView;
    @FXML public ListView<String> programListView;

    // 程序信息展示控件
    @FXML public Label programNameLabel;
    @FXML public TextField localPathField;
    @FXML public TextField updatePathField;  // 对应远程链接
    @FXML public TextArea remarksArea;       // 备注
    @FXML public TextArea targetArea;        // 新增目标控件

    // 按钮控件
    @FXML public Button browseLocalBtn;
    @FXML public Button browseUpdateBtn;
    @FXML public Button startBtn;
    @FXML public Button saveBtn;
    //    @FXML public Button genCmdBtn;
    @FXML public Button helpBtn;
    @FXML public Button openFolderBtn;

    // 启动参数相关控件
    @FXML public RadioButton param1Radio;
    @FXML public RadioButton param2Radio;
    @FXML public RadioButton param3Radio;
    @FXML public TextField paramsField1;
    @FXML public TextField paramsField2;
    @FXML public TextField paramsField3;

    // 服务依赖
    private final ControllerService controllerService;
    private final EventService eventService;
    private final MenuService menuService;

    public WinCmdRunController() {
        this.controllerService = new DefaultControllerService(this);
        this.eventService = new EventServiceImpl(this);
        this.menuService = new MenuServiceImpl(this);
    }

    // 提供controllerService的公共访问方法
    public ControllerService getControllerService() {
        return controllerService;
    }

    @FXML
    public void initialize() {
        // 初始化参数选择框分组
        ToggleGroup paramGroup = new ToggleGroup();
        param1Radio.setToggleGroup(paramGroup);
        param2Radio.setToggleGroup(paramGroup);
        param3Radio.setToggleGroup(paramGroup);
        param1Radio.setSelected(true);  // 默认选择参数1

        // 代码中设置带换行的提示文本
        String prompt = "这里是用来填充变量的，格式为：127.0.0.1@80，或者 http://127.0.0.1:18080\r\n" +
                "仅使用“@”符号进行[ip]和[port]的分割\r\n" +
                "使用示例1：python poc.py http://127.0.0.1:18080\r\n" +
                "启动参数为：python [file] [ip]\r\n" +
                "使用示例2：python poc.py 127.0.0.1 18080\r\n" +
                "启动参数为：python [file] -ip [ip] -p [port] \r\n"
                ;
        targetArea.setPromptText(prompt);

        // 初始化服务
        controllerService.initialize();
        eventService.bindEvents();
        menuService.initContextMenus();
    }

    // 配置相关方法
    public Config getConfig() {
        return controllerService.getConfig();
    }

    public void refreshGroupList() {
        controllerService.refreshGroupList();
    }

    public void refreshProgramList() {
        controllerService.refreshProgramList();
    }

}