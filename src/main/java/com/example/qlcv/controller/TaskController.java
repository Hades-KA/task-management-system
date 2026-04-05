package com.example.qlcv.controller;

import com.example.qlcv.entity.Task;
import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import com.example.qlcv.enums.TaskType;
import com.example.qlcv.service.ProjectService;
import com.example.qlcv.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping("/projects/{projectId}/tasks/new")
    public String createForm(@PathVariable Long projectId, Principal principal, Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("projectId", projectId);
        model.addAttribute("mode", "create");
        model.addAttribute("allStatuses", TaskStatus.values());
        model.addAttribute("allPriorities", TaskPriority.values());
        model.addAttribute("allTaskTypes", TaskType.values());
        model.addAttribute("participants", projectService.getParticipants(projectId, principal.getName()));
        return "tasks/form";
    }

    @PostMapping("/projects/{projectId}/tasks")
    public String create(@PathVariable Long projectId, Principal principal,
                         @Valid @ModelAttribute("task") Task task,
                         BindingResult br,
                         @RequestParam(required = false) Long assigneeId,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("projectId", projectId);
            model.addAttribute("mode", "create");
            model.addAttribute("allStatuses", TaskStatus.values());
            model.addAttribute("allPriorities", TaskPriority.values());
            model.addAttribute("allTaskTypes", TaskType.values());
            model.addAttribute("participants", projectService.getParticipants(projectId, principal.getName()));
            return "tasks/form";
        }
        taskService.create(projectId, principal.getName(), task, assigneeId);
        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/tasks/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        Task t = taskService.getAccessibleOrThrow(id, principal.getName());
        model.addAttribute("task", t);
        model.addAttribute("projectId", t.getProject().getId());
        model.addAttribute("mode", "edit");
        model.addAttribute("allStatuses", TaskStatus.values());
        model.addAttribute("allPriorities", TaskPriority.values());
        model.addAttribute("allTaskTypes", TaskType.values());
        model.addAttribute("participants", projectService.getParticipants(t.getProject().getId(), principal.getName()));
        return "tasks/form";
    }

    @PostMapping("/tasks/{id}/edit")
    public String update(@PathVariable Long id, Principal principal,
                         @RequestParam Long projectId,
                         @Valid @ModelAttribute("task") Task task,
                         BindingResult br,
                         @RequestParam(required = false) Long assigneeId,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("projectId", projectId);
            model.addAttribute("mode", "edit");
            model.addAttribute("allStatuses", TaskStatus.values());
            model.addAttribute("allPriorities", TaskPriority.values());
            model.addAttribute("allTaskTypes", TaskType.values());
            model.addAttribute("participants", projectService.getParticipants(projectId, principal.getName()));
            return "tasks/form";
        }
        taskService.update(id, principal.getName(), task, assigneeId);
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, @RequestParam Long projectId) {
        taskService.delete(id, principal.getName());
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/tasks/{id}/status")
    public String status(@PathVariable Long id, Principal principal,
                         @RequestParam Long projectId,
                         @RequestParam TaskStatus value) {
        taskService.updateStatus(id, principal.getName(), value);
        return "redirect:/projects/" + projectId;
    }
}