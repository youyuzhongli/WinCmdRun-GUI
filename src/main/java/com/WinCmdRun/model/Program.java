package com.WinCmdRun.model;

public class Program {
    private String name;
    private String localPath;
    private String updatePath;  // 对应远程链接
    private String remarks;     // 备注
    private String target;      // 新增目标字段
    private String group;
    private String startupParams1;
    private String startupParams2;
    private String startupParams3;

    public Program() {}

    public Program(String name, String localPath, String updatePath, String remarks, String target, String group,
                   String startupParams1, String startupParams2, String startupParams3) {
        this.name = name;
        this.localPath = localPath;
        this.updatePath = updatePath;
        this.remarks = remarks;
        this.target = target;  // 初始化目标字段
        this.group = group;
        this.startupParams1 = startupParams1;
        this.startupParams2 = startupParams2;
        this.startupParams3 = startupParams3;
    }

    // Getter和Setter（新增target的getter/setter）
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    // 其他原有字段的getter/setter保持不变
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public String getUpdatePath() { return updatePath; }
    public void setUpdatePath(String updatePath) { this.updatePath = updatePath; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public String getStartupParams1() { return startupParams1; }
    public void setStartupParams1(String startupParams1) { this.startupParams1 = startupParams1; }
    public String getStartupParams2() { return startupParams2; }
    public void setStartupParams2(String startupParams2) { this.startupParams2 = startupParams2; }
    public String getStartupParams3() { return startupParams3; }
    public void setStartupParams3(String startupParams3) { this.startupParams3 = startupParams3; }
}