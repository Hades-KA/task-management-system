package com.example.qlcv.dto;

/**
 * DTO đại diện cho 1 task AI gợi ý.
 */
public class AiTaskSuggestionDto {

    private String title;
    private String description;
    private String taskType;
    private String priority;
    private Integer estimatedDays;
    private String suggestedAssignee;
    private String suggestedReason;
    private String suggestedDueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Integer estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public String getSuggestedAssignee() {
        return suggestedAssignee;
    }

    public void setSuggestedAssignee(String suggestedAssignee) {
        this.suggestedAssignee = suggestedAssignee;
    }

    public String getSuggestedReason() {
        return suggestedReason;
    }

    public void setSuggestedReason(String suggestedReason) {
        this.suggestedReason = suggestedReason;
    }

    public String getSuggestedDueDate() {
        return suggestedDueDate;
    }

    public void setSuggestedDueDate(String suggestedDueDate) {
        this.suggestedDueDate = suggestedDueDate;
    }
}