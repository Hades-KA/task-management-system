package com.example.qlcv.dto;

import java.time.LocalDate;

/**
 * Form nhập dữ liệu cho AI planner.
 */
public class AiPlannerForm {

    private String requirement;
    private String memberContext;
    private LocalDate startDate;
    private String planJson;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getMemberContext() {
        return memberContext;
    }

    public void setMemberContext(String memberContext) {
        this.memberContext = memberContext;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getPlanJson() {
        return planJson;
    }

    public void setPlanJson(String planJson) {
        this.planJson = planJson;
    }
}