package com.WinCmdRun.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private List<Program> programs = new ArrayList<>();

    public Group() {}
    public Group(String name) {
        this.name = name;
    }

    // Getter和Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Program> getPrograms() { return programs; }
    public void setPrograms(List<Program> programs) { this.programs = programs; }
}