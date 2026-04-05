package com.example.qlcv.controller;

import com.example.qlcv.dto.CalendarDayDto;
import com.example.qlcv.entity.Task;
import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import com.example.qlcv.service.ProjectService;
import com.example.qlcv.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final TaskService taskService;
    private final ProjectService projectService;

    public UiController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    // ===== KANBAN =====
    @GetMapping("/kanban")
    public String kanban(@RequestParam(required = false) Long projectId,
                         @RequestParam(required = false) TaskPriority priority,
                         Principal principal, Model model) {

        String email = principal.getName();
        List<Task> allTasks;

        if (projectId != null) {
            allTasks = taskService.findAllAccessibleByProject(email, projectId);
        } else {
            allTasks = taskService.findAllAccessible(email);
        }

        if (priority != null) {
            allTasks = allTasks.stream()
                    .filter(t -> t.getPriority() == priority)
                    .collect(Collectors.toList());
        }

        List<Task> todoTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).collect(Collectors.toList());
        List<Task> inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).collect(Collectors.toList());
        List<Task> doneTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).collect(Collectors.toList());

        model.addAttribute("todoTasks", todoTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("doneTasks", doneTasks);

        model.addAttribute("projects", projectService.listForUser(email));
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("allPriorities", TaskPriority.values());

        return "ui/kanban";
    }

    @PostMapping("/kanban/tasks/{id}/move")
    @ResponseBody
    public ResponseEntity<?> moveTask(@PathVariable Long id,
                                      @RequestParam TaskStatus status,
                                      Principal principal) {
        taskService.updateStatus(id, principal.getName(), status);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== LIST =====
    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long projectId,
                       @RequestParam(required = false) TaskStatus status,
                       @RequestParam(required = false) TaskPriority priority,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) Boolean overdue,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Principal principal, Model model) {

        String email = principal.getName();
        List<Task> allTasks;

        if (projectId != null) {
            allTasks = taskService.findAllAccessibleByProject(email, projectId);
        } else {
            allTasks = taskService.findAllAccessible(email);
        }

        long totalCount = allTasks.size();
        long todoCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        List<Task> filtered = new ArrayList<>(allTasks);

        if (status != null) {
            filtered = filtered.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        if (priority != null) {
            filtered = filtered.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        }
        if (q != null && !q.isBlank()) {
            String kw = q.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(overdue)) {
            LocalDate today = LocalDate.now();
            filtered = filtered.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != TaskStatus.DONE)
                    .collect(Collectors.toList());
        }

        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<Task> pagedTasks = (fromIndex < totalItems) ? filtered.subList(fromIndex, toIndex) : new ArrayList<>();

        model.addAttribute("tasks", pagedTasks);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);

        model.addAttribute("projects", projectService.listForUser(email));
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("q", q);
        model.addAttribute("overdue", overdue);
        model.addAttribute("allStatuses", TaskStatus.values());
        model.addAttribute("allPriorities", TaskPriority.values());

        return "ui/list";
    }

    // ===== CALENDAR =====
    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
                           @RequestParam(required = false) Integer month,
                           @RequestParam(required = false) Long projectId,
                           @RequestParam(required = false) TaskStatus status,
                           Principal principal, Model model) {

        String email = principal.getName();

        LocalDate now = LocalDate.now();
        int currentYear = (year != null) ? year : now.getYear();
        int currentMonth = (month != null) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(currentYear, currentMonth);

        List<Task> allTasks = taskService.findAllAccessible(email);

        if (projectId != null) {
            allTasks = allTasks.stream()
                    .filter(t -> t.getProject() != null && t.getProject().getId().equals(projectId))
                    .collect(Collectors.toList());
        }

        long totalCount = allTasks.size();
        long todoCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        List<Task> filteredTasks = allTasks;
        if (status != null) {
            filteredTasks = allTasks.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }

        Map<LocalDate, List<Task>> taskMap = filteredTasks.stream()
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.groupingBy(Task::getDueDate));

        LocalDate firstDayOfMonth = ym.atDay(1);
        int shift = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate startDate = firstDayOfMonth.minusDays(shift);

        List<CalendarDayDto> days = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate date = startDate.plusDays(i);
            days.add(new CalendarDayDto(
                    date,
                    date.getMonthValue() == ym.getMonthValue(),
                    date.equals(now),
                    taskMap.getOrDefault(date, new ArrayList<>())
            ));
        }

        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        model.addAttribute("calendarDays", days);
        model.addAttribute("monthLabel", "Tháng " + ym.getMonthValue() + " " + ym.getYear());
        model.addAttribute("year", ym.getYear());
        model.addAttribute("month", ym.getMonthValue());
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());

        model.addAttribute("projects", projectService.listForUser(email));
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);

        return "ui/calendar";
    }

    // ===== HISTORY =====
    @GetMapping("/history")
    public String history(Principal principal, Model model) {
        String email = principal.getName();
        model.addAttribute("logs", taskService.getActivityLogs(email));
        return "ui/history";
    }
}