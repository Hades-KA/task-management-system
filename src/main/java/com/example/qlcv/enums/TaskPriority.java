package com.example.qlcv.enums;

public enum TaskPriority {
    LOW("Thấp", "low"),
    MEDIUM("Trung bình", "med"),
    HIGH("Cao", "high");

    private final String label;
    private final String cssClass;

    TaskPriority(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }
}