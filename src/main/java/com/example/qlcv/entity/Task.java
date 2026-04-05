package com.example.qlcv.entity;

import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import com.example.qlcv.enums.TaskType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề task không được để trống")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    // === JIRA MINI: Loại công việc ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType = TaskType.TASK;

    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // === JIRA MINI: Người tạo ===
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    private AppUser reporter;

    // === JIRA MINI: Người phụ trách ===
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public AppUser getReporter() { return reporter; }
    public void setReporter(AppUser reporter) { this.reporter = reporter; }

    public AppUser getAssignee() { return assignee; }
    public void setAssignee(AppUser assignee) { this.assignee = assignee; }

    // === Helper ===
    public String getReporterName() {
        return reporter != null ? reporter.getFullName() : "Không rõ";
    }

    public String getAssigneeName() {
        return assignee != null ? assignee.getFullName() : "Chưa giao";
    }
}