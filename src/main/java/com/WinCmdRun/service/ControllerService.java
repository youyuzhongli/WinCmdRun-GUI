package com.WinCmdRun.service;

import com.WinCmdRun.model.Config;

public interface ControllerService {
    void initialize();
    Config getConfig();
    void refreshGroupList();
    void refreshProgramList();

}