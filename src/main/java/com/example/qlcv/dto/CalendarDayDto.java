package com.example.qlcv.dto;

import com.example.qlcv.entity.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDayDto {
    private LocalDate date;
    private boolean currentMonth;
    private boolean today;
    private List<Task> tasks = new ArrayList<>();

    public CalendarDayDto(LocalDate date, boolean currentMonth, boolean today, List<Task> tasks) {
        this.date = date;
        this.currentMonth = currentMonth;
        this.today = today;
        this.tasks = tasks;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isCurrentMonth() {
        return currentMonth;
    }

    public boolean isToday() {
        return today;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}