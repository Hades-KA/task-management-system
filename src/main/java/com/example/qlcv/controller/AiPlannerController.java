package com.example.qlcv.controller;

import com.example.qlcv.dto.AiPlanResponseDto;
import com.example.qlcv.dto.AiPlannerForm;
import com.example.qlcv.dto.AiTaskSuggestionDto;
import com.example.qlcv.entity.AppUser;
import com.example.qlcv.entity.Project;
import com.example.qlcv.entity.Task;
import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskType;
import com.example.qlcv.service.GeminiAiService;
import com.example.qlcv.service.ProjectService;
import com.example.qlcv.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectId}/ai-planner")
public class AiPlannerController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final GeminiAiService geminiAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiPlannerController(ProjectService projectService,
                               TaskService taskService,
                               GeminiAiService geminiAiService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.geminiAiService = geminiAiService;
    }

    @GetMapping
    public String page(@PathVariable Long projectId, Principal principal, Model model) {
        String email = principal.getName();
        Project project = projectService.getForUserOrThrow(projectId, email);

        AiPlannerForm form = new AiPlannerForm();
        form.setStartDate(LocalDate.now());

        // Tự sinh context đẹp hơn từ toàn bộ participant của project
        form.setMemberContext(buildMemberContext(projectService.getParticipants(projectId, email)));

        model.addAttribute("project", project);
        model.addAttribute("form", form);
        return "projects/ai-planner";
    }

    @PostMapping("/generate")
    public String generate(@PathVariable Long projectId,
                           Principal principal,
                           @ModelAttribute("form") AiPlannerForm form,
                           Model model) {
        String email = principal.getName();
        Project project = projectService.getForUserOrThrow(projectId, email);

        model.addAttribute("project", project);

        try {
            if (form.getRequirement() == null || form.getRequirement().isBlank()) {
                model.addAttribute("error", "Vui lòng nhập mô tả yêu cầu dự án để AI phân tích.");
                model.addAttribute("form", form);
                return "projects/ai-planner";
            }

            AiPlanResponseDto plan = geminiAiService.generatePlan(
                    project.getName(),
                    project.getDescription(),
                    form.getStartDate(),
                    form.getRequirement(),
                    form.getMemberContext()
            );

            applySuggestedDueDates(plan, form.getStartDate());
            form.setPlanJson(objectMapper.writeValueAsString(plan));

            model.addAttribute("form", form);
            model.addAttribute("plan", plan);
            return "projects/ai-planner";

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Có lỗi xảy ra khi gọi AI.";
            }
            model.addAttribute("form", form);
            model.addAttribute("error", msg);
            return "projects/ai-planner";
        }
    }

    @PostMapping("/import")
    public String importPlan(@PathVariable Long projectId,
                             Principal principal,
                             @ModelAttribute("form") AiPlannerForm form,
                             RedirectAttributes ra) {
        String email = principal.getName();

        try {
            if (form.getPlanJson() == null || form.getPlanJson().isBlank()) {
                ra.addFlashAttribute("errorMessage", "Không có dữ liệu AI để import.");
                return "redirect:/projects/" + projectId + "/ai-planner";
            }

            AiPlanResponseDto plan = objectMapper.readValue(form.getPlanJson(), AiPlanResponseDto.class);
            Set<AppUser> participants = projectService.getParticipants(projectId, email);

            LocalDate cursor = form.getStartDate() != null ? form.getStartDate() : LocalDate.now();
            int imported = 0;

            for (AiTaskSuggestionDto item : plan.getTasks()) {
                if (item.getTitle() == null || item.getTitle().isBlank()) continue;

                int estimated = normalizeEstimatedDays(item.getEstimatedDays());
                LocalDate dueDate = cursor.plusDays(estimated - 1L);

                Task t = new Task();
                t.setTitle(item.getTitle());
                t.setDescription(buildFinalDescription(item));
                t.setTaskType(parseTaskType(item.getTaskType()));
                t.setPriority(parsePriority(item.getPriority()));
                t.setDueDate(dueDate);

                Long assigneeId = resolveAssigneeId(item.getSuggestedAssignee(), participants);

                taskService.create(projectId, email, t, assigneeId);
                imported++;

                cursor = dueDate.plusDays(1);
            }

            ra.addFlashAttribute("successMessage", "AI đã tạo " + imported + " công việc cho dự án.");
            return "redirect:/projects/" + projectId;

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Import thất bại: " + e.getMessage());
            return "redirect:/projects/" + projectId + "/ai-planner";
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public String testAi(@PathVariable Long projectId, Principal principal) {
        String email = principal.getName();
        Project project = projectService.getForUserOrThrow(projectId, email);

        AiPlanResponseDto plan = geminiAiService.generatePlan(
                project.getName(),
                project.getDescription(),
                LocalDate.now(),
                "Tạo kế hoạch xây dựng website bán hàng trong 4 tuần với các chức năng đăng ký, đăng nhập, dashboard, quản lý sản phẩm.",
                buildMemberContext(projectService.getParticipants(projectId, email))
        );

        return "OK - AI sinh ra " + (plan.getTasks() != null ? plan.getTasks().size() : 0) + " task";
    }

    private void applySuggestedDueDates(AiPlanResponseDto plan, LocalDate startDate) {
        LocalDate cursor = startDate != null ? startDate : LocalDate.now();

        if (plan.getTasks() == null) return;

        for (AiTaskSuggestionDto item : plan.getTasks()) {
            int estimated = normalizeEstimatedDays(item.getEstimatedDays());
            LocalDate dueDate = cursor.plusDays(estimated - 1L);
            item.setSuggestedDueDate(dueDate.toString());
            cursor = dueDate.plusDays(1);
        }
    }

    private int normalizeEstimatedDays(Integer days) {
        if (days == null || days <= 0) return 1;
        if (days > 5) return 5;
        return days;
    }

    private String buildFinalDescription(AiTaskSuggestionDto item) {
        String desc = item.getDescription() != null ? item.getDescription().trim() : "";
        String est = "\n\n[Ước lượng AI: " + normalizeEstimatedDays(item.getEstimatedDays()) + " ngày]";
        return desc + est;
    }

    private TaskType parseTaskType(String value) {
        if (value == null) return TaskType.TASK;
        try {
            return TaskType.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return TaskType.TASK;
        }
    }

    private TaskPriority parsePriority(String value) {
        if (value == null) return TaskPriority.MEDIUM;
        try {
            return TaskPriority.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return TaskPriority.MEDIUM;
        }
    }

    private Long resolveAssigneeId(String suggestedAssignee, Set<AppUser> participants) {
        if (suggestedAssignee == null || suggestedAssignee.isBlank()) return null;

        String keyword = suggestedAssignee.trim().toLowerCase();

        for (AppUser u : participants) {
            if (u.getFullName() != null && u.getFullName().trim().toLowerCase().equals(keyword)) {
                return u.getId();
            }
            if (u.getEmail() != null && u.getEmail().trim().toLowerCase().equals(keyword)) {
                return u.getId();
            }
        }

        for (AppUser u : participants) {
            if (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword)) {
                return u.getId();
            }
        }

        return null;
    }

    /**
     * Sinh context mặc định thông minh cho AI Planner
     * - Tự lấy toàn bộ thành viên project
     * - Tự gán role/skill mặc định theo user
     * - Tự sinh câu gợi ý phân công
     */
    private String buildMemberContext(Set<AppUser> participants) {
        List<AppUser> users = participants.stream()
                .sorted(Comparator.comparing(AppUser::getFullName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Danh sách thành viên hiện có:\n");

        for (AppUser u : users) {
            sb.append("- ")
              .append(safe(u.getFullName()))
              .append(" (")
              .append(safe(u.getEmail()))
              .append("): ")
              .append(suggestSkills(u))
              .append("\n");
        }

        sb.append("\n");
        sb.append(buildAssignmentHint(users));

        return sb.toString().trim();
    }

    /**
     * Gợi ý skill mặc định theo email / tên.
     * Với user lạ thì dùng preset mềm.
     */
    private String suggestSkills(AppUser user) {
        String email = safe(user.getEmail()).toLowerCase();
        String name = safe(user.getFullName()).toLowerCase();

        // Preset cố định cho seed data
        if (email.contains("admin") || name.contains("admin")) {
            return "backend, spring boot, database, security";
        }
        if (email.equals("user@gmail.com") || name.contains("nguyễn văn a")) {
            return "frontend, html, css, javascript, UI";
        }
        if (email.contains("tranb") || name.contains("trần thị b")) {
            return "testing, kiểm thử, viết tài liệu";
        }
        if (email.contains("levanc") || name.contains("lê văn c")) {
            return "mobile, API integration, testing hỗ trợ";
        }

        // Fallback mềm cho user khác
        String[] presets = {
                "frontend, html, css, UI",
                "backend, database, API",
                "testing, kiểm thử, viết tài liệu",
                "mobile, UI, API integration"
        };

        int idx = Math.abs(email.hashCode()) % presets.length;
        return presets[idx];
    }

    /**
     * Sinh câu gợi ý phân công ở cuối context.
     */
    private String buildAssignmentHint(List<AppUser> users) {
        List<String> hints = new ArrayList<>();

        for (AppUser u : users) {
            String email = safe(u.getEmail()).toLowerCase();
            String name = safe(u.getFullName());

            String skills = suggestSkills(u).toLowerCase();

            if (skills.contains("frontend") || skills.contains("ui")) {
                hints.add("task giao diện cho " + name);
            }
            if (skills.contains("backend") || skills.contains("security")) {
                hints.add("task backend/bảo mật cho " + name);
            }
            if (skills.contains("testing") || skills.contains("kiểm thử")) {
                hints.add("task kiểm thử cho " + name);
            }
            if (skills.contains("mobile") || skills.contains("api integration")) {
                hints.add("task mobile/API cho " + name);
            }
        }

        if (hints.isEmpty()) {
            return "Hãy gợi ý người phụ trách phù hợp dựa trên loại công việc và mức độ ưu tiên.";
        }

        // loại bỏ trùng
        List<String> distinctHints = hints.stream().distinct().toList();

        return "Hãy ưu tiên giao " + String.join(", ", distinctHints) + " nếu phù hợp.";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}