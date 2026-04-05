package com.example.qlcv.service;

import com.example.qlcv.entity.ActivityLog;
import com.example.qlcv.entity.AppUser;
import com.example.qlcv.entity.Project;
import com.example.qlcv.entity.Task;
import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import com.example.qlcv.enums.TaskType;
import com.example.qlcv.repository.TaskRepository;
import com.example.qlcv.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final ProjectService projectService;
    private final ActivityLogService activityLogService;

    public TaskService(TaskRepository taskRepo, UserRepository userRepo,
                       ProjectService projectService, ActivityLogService activityLogService) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.projectService = projectService;
        this.activityLogService = activityLogService;
    }

    // ========== DANH SÁCH ==========

    public List<Task> listByProject(Long projectId, String email,
                                    String q, TaskStatus status, TaskPriority priority,
                                    TaskType taskType, Long assigneeId, Boolean overdue) {
        projectService.getForUserOrThrow(projectId, email);

        List<Task> tasks = taskRepo.findAccessibleByProjectId(projectId, email);

        if (StringUtils.hasText(q)) {
            String kw = q.toLowerCase(Locale.ROOT).trim();
            tasks = tasks.stream()
                    .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase(Locale.ROOT).contains(kw))
                    .collect(Collectors.toList());
        }
        if (status != null) {
            tasks = tasks.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        if (priority != null) {
            tasks = tasks.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        }
        if (taskType != null) {
            tasks = tasks.stream().filter(t -> t.getTaskType() == taskType).collect(Collectors.toList());
        }
        if (assigneeId != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(assigneeId))
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(overdue)) {
            LocalDate today = LocalDate.now();
            tasks = tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != TaskStatus.DONE)
                    .collect(Collectors.toList());
        }

        return tasks;
    }

    public List<Task> findAllAccessible(String email) {
        return taskRepo.findAllAccessible(email);
    }

    public List<Task> findAllAccessibleByProject(String email, Long projectId) {
        projectService.getForUserOrThrow(projectId, email);
        return taskRepo.findAccessibleByProjectId(projectId, email);
    }

    public Task getAccessibleOrThrow(Long taskId, String email) {
        return taskRepo.findAccessibleById(taskId, email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy task hoặc không có quyền"));
    }

    // ========== CRUD ==========

    public Task create(Long projectId, String email, Task form, Long assigneeId) {
        Project project = projectService.getForUserOrThrow(projectId, email);
        AppUser reporter = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        Task t = new Task();
        t.setTitle(form.getTitle());
        t.setDescription(form.getDescription());
        t.setStatus(TaskStatus.TODO);
        t.setPriority(form.getPriority() == null ? TaskPriority.MEDIUM : form.getPriority());
        t.setTaskType(form.getTaskType() == null ? TaskType.TASK : form.getTaskType());
        t.setDueDate(form.getDueDate());
        t.setProject(project);
        t.setReporter(reporter);

        // Giao người phụ trách
        if (assigneeId != null) {
            AppUser assignee = userRepo.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user phụ trách"));
            t.setAssignee(assignee);
        }

        t = taskRepo.save(t);

        activityLogService.log(email, "CREATE", "TASK",
                t.getId(), t.getTitle(),
                "Tạo " + t.getTaskType().getLabel() + " trong project \"" + project.getName() + "\""
                        + (t.getAssignee() != null ? " — Giao cho: " + t.getAssigneeName() : ""));

        return t;
    }

    public Task update(Long taskId, String email, Task form, Long assigneeId) {
        Task t = getAccessibleOrThrow(taskId, email);

        String changes = buildChanges(t, form, assigneeId);

        t.setTitle(form.getTitle());
        t.setDescription(form.getDescription());
        t.setStatus(form.getStatus());
        t.setPriority(form.getPriority());
        t.setTaskType(form.getTaskType() == null ? t.getTaskType() : form.getTaskType());
        t.setDueDate(form.getDueDate());

        if (assigneeId != null) {
            AppUser assignee = userRepo.findById(assigneeId).orElse(null);
            t.setAssignee(assignee);
        } else {
            t.setAssignee(null);
        }

        t = taskRepo.save(t);

        activityLogService.log(email, "UPDATE", "TASK", t.getId(), t.getTitle(), changes);

        return t;
    }

    public void delete(Long taskId, String email) {
        Task t = getAccessibleOrThrow(taskId, email);
        String title = t.getTitle();
        Long id = t.getId();
        String projectName = t.getProject().getName();

        taskRepo.delete(t);

        activityLogService.log(email, "DELETE", "TASK", id, title,
                "Xóa task khỏi project \"" + projectName + "\"");
    }

    public void updateStatus(Long taskId, String email, TaskStatus status) {
        Task t = getAccessibleOrThrow(taskId, email);
        TaskStatus oldStatus = t.getStatus();
        t.setStatus(status);
        taskRepo.save(t);

        activityLogService.log(email, "STATUS_CHANGE", "TASK",
                t.getId(), t.getTitle(),
                oldStatus.getLabel() + " → " + status.getLabel());
    }

    public void assignTask(Long taskId, String email, Long assigneeId) {
        Task t = getAccessibleOrThrow(taskId, email);
        String oldAssignee = t.getAssigneeName();

        if (assigneeId != null) {
            AppUser assignee = userRepo.findById(assigneeId).orElse(null);
            t.setAssignee(assignee);
        } else {
            t.setAssignee(null);
        }
        taskRepo.save(t);

        activityLogService.log(email, "ASSIGN", "TASK",
                t.getId(), t.getTitle(),
                "Đổi phụ trách: " + oldAssignee + " → " + t.getAssigneeName());
    }

    // ========== ACTIVITY LOG ==========

    public List<ActivityLog> getActivityLogs(String email) {
        return activityLogService.getRecentLogs(email);
    }

    // ========== HELPER ==========

    private String buildChanges(Task old, Task form, Long newAssigneeId) {
        StringBuilder sb = new StringBuilder();

        if (!old.getTitle().equals(form.getTitle())) {
            sb.append("Tiêu đề: \"").append(old.getTitle()).append("\" → \"").append(form.getTitle()).append("\". ");
        }
        if (old.getStatus() != form.getStatus()) {
            sb.append("Status: ").append(old.getStatus().getLabel()).append(" → ").append(form.getStatus().getLabel()).append(". ");
        }
        if (old.getPriority() != form.getPriority()) {
            sb.append("Priority: ").append(old.getPriority().getLabel()).append(" → ").append(form.getPriority().getLabel()).append(". ");
        }

        Long oldAssigneeId = old.getAssignee() != null ? old.getAssignee().getId() : null;
        if ((oldAssigneeId == null && newAssigneeId != null) ||
                (oldAssigneeId != null && !oldAssigneeId.equals(newAssigneeId))) {
            sb.append("Phụ trách: ").append(old.getAssigneeName()).append(" → thay đổi. ");
        }

        return sb.length() > 0 ? sb.toString().trim() : "Cập nhật thông tin task";
    }
}