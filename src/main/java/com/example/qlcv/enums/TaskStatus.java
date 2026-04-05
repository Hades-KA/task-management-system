package com.example.qlcv.enums;

public enum TaskStatus {

    TODO("Cần làm", "bg-secondary-lt text-secondary", "ti ti-circle"),
    IN_PROGRESS("Đang làm", "bg-primary-lt text-primary", "ti ti-progress"),
    DONE("Hoàn thành", "bg-success-lt text-success", "ti ti-circle-check");

    private final String label;
    private final String badgeClass;
    private final String iconClass;

    TaskStatus(String label, String badgeClass, String iconClass) {
        this.label = label;
        this.badgeClass = badgeClass;
        this.iconClass = iconClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getIconClass() {
        return iconClass;
    }
}