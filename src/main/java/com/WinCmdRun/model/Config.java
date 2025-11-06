package com.WinCmdRun.model;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private List<String> groups = new ArrayList<>();
    private List<Program> programs = new ArrayList<>();

    public List<String> getGroups() { return groups; }
    public void setGroups(List<String> groups) { this.groups = groups; }
    public List<Program> getPrograms() { return programs; }
    public void setPrograms(List<Program> programs) { this.programs = programs; }
}