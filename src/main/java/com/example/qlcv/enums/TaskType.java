package com.example.qlcv.enums;

public enum TaskType {

    TASK("Công việc", "ti ti-subtask", "#3b82f6"),
    BUG("Lỗi", "ti ti-bug", "#fb7185"),
    FEATURE("Tính năng", "ti ti-bulb-filled", "#fbbf24");

    private final String label;
    private final String iconClass;
    private final String color;

    TaskType(String label, String iconClass, String color) {
        this.label = label;
        this.iconClass = iconClass;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getIconClass() { return iconClass; }
    public String getColor() { return color; }
}