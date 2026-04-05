package com.example.qlcv.controller;

import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import com.example.qlcv.repository.TaskRepository;
import com.example.qlcv.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Controller
public class DashboardController {

    private final ProjectService projectService;
    private final TaskRepository taskRepo;

    public DashboardController(ProjectService projectService, TaskRepository taskRepo) {
        this.projectService = projectService;
        this.taskRepo = taskRepo;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String email = principal.getName();

        long projects = projectService.countProjects(email);
        long tasks = taskRepo.countAccessible(email);
        long overdue = taskRepo.countAccessibleOverdue(email, LocalDate.now());

        Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) byStatus.put(s, 0L);
        for (Object[] row : taskRepo.countAccessibleByStatus(email)) {
            byStatus.put((TaskStatus) row[0], (Long) row[1]);
        }

        long todoCount = byStatus.getOrDefault(TaskStatus.TODO, 0L);
        long inProgressCount = byStatus.getOrDefault(TaskStatus.IN_PROGRESS, 0L);
        long doneCount = byStatus.getOrDefault(TaskStatus.DONE, 0L);

        long lowCount = taskRepo.countAccessibleByPriority(email, TaskPriority.LOW);
        long mediumCount = taskRepo.countAccessibleByPriority(email, TaskPriority.MEDIUM);
        long highCount = taskRepo.countAccessibleByPriority(email, TaskPriority.HIGH);

        long completionRate = tasks > 0 ? Math.round((double) doneCount / tasks * 100) : 0;

        model.addAttribute("projectsCount", projects);
        model.addAttribute("tasksCount", tasks);
        model.addAttribute("overdueCount", overdue);

        model.addAttribute("todoCount", todoCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);

        model.addAttribute("lowCount", lowCount);
        model.addAttribute("mediumCount", mediumCount);
        model.addAttribute("highCount", highCount);

        model.addAttribute("completionRate", completionRate);
        model.addAttribute("recentTasks", taskRepo.findAccessibleRecentTasks(email).stream().limit(8).toList());

        return "dashboard";
    }
}