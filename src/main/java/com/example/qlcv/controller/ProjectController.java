package com.example.qlcv.controller;

import com.example.qlcv.entity.Project;
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
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("projects", projectService.listForUser(principal.getName()));
        return "projects/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("mode", "create");
        return "projects/form";
    }

    @PostMapping
    public String create(Principal principal,
                         @Valid @ModelAttribute("project") Project project,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "projects/form";
        }
        projectService.createForUser(project, principal.getName());
        return "redirect:/projects";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Principal principal,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) TaskStatus status,
                         @RequestParam(required = false) TaskPriority priority,
                         @RequestParam(required = false) TaskType taskType,
                         @RequestParam(required = false) Long assigneeId,
                         @RequestParam(required = false) Boolean overdue,
                         Model model) {

        String email = principal.getName();
        model.addAttribute("project", projectService.getForUserOrThrow(id, email));
        model.addAttribute("tasks", taskService.listByProject(id, email, q, status, priority, taskType, assigneeId, overdue));
        model.addAttribute("participants", projectService.getParticipants(id, email));

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("taskType", taskType);
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("overdue", overdue);

        model.addAttribute("allStatuses", TaskStatus.values());
        model.addAttribute("allPriorities", TaskPriority.values());
        model.addAttribute("allTaskTypes", TaskType.values());
        return "projects/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("project", projectService.getForUserOrThrow(id, principal.getName()));
        model.addAttribute("mode", "edit");
        return "projects/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Principal principal,
                         @Valid @ModelAttribute("project") Project project,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "projects/form";
        }
        projectService.updateForUser(id, project, principal.getName());
        return "redirect:/projects";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        projectService.deleteForUser(id, principal.getName());
        return "redirect:/projects";
    }

    // ===== QUẢN LÝ THÀNH VIÊN =====
    @PostMapping("/{id}/members/add")
    public String addMember(@PathVariable Long id,
                            @RequestParam String memberEmail,
                            Principal principal) {
        projectService.addMember(id, memberEmail, principal.getName());
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/members/{memberId}/remove")
    public String removeMember(@PathVariable Long id,
                               @PathVariable Long memberId,
                               Principal principal) {
        projectService.removeMember(id, memberId, principal.getName());
        return "redirect:/projects/" + id;
    }
}