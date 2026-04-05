package com.example.qlcv.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa toàn bộ kết quả AI trả về.
 */
public class AiPlanResponseDto {

    private String summary;
    private String timelineSummary;
    private List<AiTaskSuggestionDto> tasks = new ArrayList<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTimelineSummary() {
        return timelineSummary;
    }

    public void setTimelineSummary(String timelineSummary) {
        this.timelineSummary = timelineSummary;
    }

    public List<AiTaskSuggestionDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<AiTaskSuggestionDto> tasks) {
        this.tasks = tasks;
    }
}