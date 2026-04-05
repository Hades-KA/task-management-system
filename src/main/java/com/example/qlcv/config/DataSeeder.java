package com.example.qlcv.config;

import com.example.qlcv.entity.*;
import com.example.qlcv.enums.*;
import com.example.qlcv.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    private final CommentRepository commentRepo;
    private final ChecklistItemRepository checklistRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo, ProjectRepository projectRepo,
                      TaskRepository taskRepo, CommentRepository commentRepo,
                      ChecklistItemRepository checklistRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.taskRepo = taskRepo;
        this.commentRepo = commentRepo;
        this.checklistRepo = checklistRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return;

        // ===== TẠO USERS =====
        AppUser admin = createUser("Admin", "admin@gmail.com", "123456", Role.ADMIN);
        AppUser userA = createUser("Nguyễn Văn A", "user@gmail.com", "123456", Role.USER);
        AppUser userB = createUser("Trần Thị B", "tranb@gmail.com", "123456", Role.USER);
        AppUser userC = createUser("Lê Văn C", "levanc@gmail.com", "123456", Role.USER);

        // ===== TẠO PROJECT 1: admin là owner, userA + userB là member =====
        Project p1 = createProject("Đồ án J2EE", "Website quản lý công việc cuối kỳ", admin);
        p1.addMember(userA);
        p1.addMember(userB);
        projectRepo.save(p1);

        // ===== TẠO PROJECT 2: admin là owner, userC là member =====
        Project p2 = createProject("Dự án Mobile App", "Ứng dụng quản lý task trên điện thoại", admin);
        p2.addMember(userC);
        projectRepo.save(p2);

        // ===== TẠO PROJECT 3: userA là owner, admin là member =====
        Project p3 = createProject("Bài tập lớn OOP", "Project OOP học kỳ 2", userA);
        p3.addMember(admin);
        p3.addMember(userC);
        projectRepo.save(p3);

        LocalDate today = LocalDate.now();

        // ===== TASK CHO P1 =====
        Task t1 = createTask("Thiết kế cơ sở dữ liệu", "Vẽ ERD + mapping JPA entity",
                TaskStatus.DONE, TaskPriority.HIGH, TaskType.TASK,
                today.minusDays(5), p1, admin, userA);

        Task t2 = createTask("Làm chức năng đăng nhập", "Spring Security + Thymeleaf",
                TaskStatus.DONE, TaskPriority.HIGH, TaskType.FEATURE,
                today.minusDays(3), p1, admin, admin);

        Task t3 = createTask("CRUD Project + Task", "Tạo/sửa/xóa project và task",
                TaskStatus.DONE, TaskPriority.MEDIUM, TaskType.TASK,
                today.minusDays(2), p1, admin, userB);

        Task t4 = createTask("Thiết kế giao diện Dashboard", "Dark theme + biểu đồ Chart.js",
                TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, TaskType.FEATURE,
                today, p1, admin, userA);

        Task t5 = createTask("Triển khai Kanban Board", "SortableJS + kéo thả card",
                TaskStatus.IN_PROGRESS, TaskPriority.HIGH, TaskType.FEATURE,
                today.plusDays(1), p1, userA, userA);

        Task t6 = createTask("Fix lỗi login khi nhập sai email", "Trả về thông báo lỗi rõ ràng",
                TaskStatus.TODO, TaskPriority.HIGH, TaskType.BUG,
                today.plusDays(2), p1, userB, admin);

        Task t7 = createTask("Trang Admin quản lý user", "Enable/disable + đổi role",
                TaskStatus.TODO, TaskPriority.MEDIUM, TaskType.TASK,
                today.plusDays(3), p1, admin, userB);

        Task t8 = createTask("Viết báo cáo đồ án", "ERD + Use case + ảnh chụp màn hình",
                TaskStatus.TODO, TaskPriority.LOW, TaskType.TASK,
                today.plusDays(5), p1, admin, null);

        Task t9 = createTask("Test toàn bộ chức năng", "Kiểm tra login/CRUD/filter",
                TaskStatus.TODO, TaskPriority.HIGH, TaskType.TASK,
                today.plusDays(4), p1, admin, userA);

        Task t10 = createTask("Lỗi hiển thị calendar trên mobile", "Responsive bị vỡ layout",
                TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, TaskType.BUG,
                today.plusDays(1), p1, userB, userB);

        // ===== TASK CHO P2 =====
        Task t11 = createTask("Thiết kế wireframe mobile", "Figma mockup cho app",
                TaskStatus.TODO, TaskPriority.MEDIUM, TaskType.TASK,
                today.plusDays(6), p2, admin, userC);

        Task t12 = createTask("Setup React Native project", "Tạo project + cài thư viện",
                TaskStatus.TODO, TaskPriority.HIGH, TaskType.TASK,
                today.plusDays(8), p2, admin, admin);

        Task t13 = createTask("API endpoint cho task", "REST API Spring Boot",
                TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, TaskType.FEATURE,
                today.plusDays(3), p2, userC, userC);

        // ===== TASK CHO P3 =====
        Task t14 = createTask("Thiết kế class diagram", "Vẽ UML class diagram",
                TaskStatus.TODO, TaskPriority.MEDIUM, TaskType.TASK,
                today.plusDays(2), p3, userA, admin);

        Task t15 = createTask("Code module quản lý sinh viên", "CRUD sinh viên bằng Java",
                TaskStatus.IN_PROGRESS, TaskPriority.HIGH, TaskType.FEATURE,
                today.plusDays(4), p3, userA, userC);

        // ===== COMMENTS =====
        createComment(t4, admin, "Dashboard đã xong phần stats, đang làm chart");
        createComment(t4, userA, "Tôi sẽ review chart khi xong nhé");
        createComment(t5, userA, "Kanban đã có drag-drop, cần test thêm");
        createComment(t6, userB, "Lỗi này xảy ra khi email không tồn tại trong DB");
        createComment(t6, admin, "OK tôi sẽ fix trong sprint này");
        createComment(t10, userB, "Đã fix 80%, còn phần header chưa ổn");

        // ===== CHECKLIST =====
        createChecklist(t1, "Vẽ ERD trên draw.io", true);
        createChecklist(t1, "Mapping Entity JPA", true);
        createChecklist(t1, "Tạo bảng MySQL", true);

        createChecklist(t4, "Layout stat cards", true);
        createChecklist(t4, "Biểu đồ Doughnut", true);
        createChecklist(t4, "Biểu đồ Bar", false);
        createChecklist(t4, "Bảng recent tasks", false);

        createChecklist(t5, "Tạo 3 cột Kanban", true);
        createChecklist(t5, "Load task từ DB", true);
        createChecklist(t5, "Drag & drop lưu DB", false);

        createChecklist(t8, "Viết phần giới thiệu", false);
        createChecklist(t8, "Chụp ảnh màn hình", false);
        createChecklist(t8, "Vẽ Use Case", false);
        createChecklist(t8, "Xuất PDF", false);
    }

    // ===== HELPER METHODS =====

    private AppUser createUser(String name, String email, String password, Role role) {
        AppUser u = new AppUser();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setEnabled(true);
        return userRepo.save(u);
    }

    private Project createProject(String name, String desc, AppUser owner) {
        Project p = new Project();
        p.setName(name);
        p.setDescription(desc);
        p.setOwner(owner);
        return projectRepo.save(p);
    }

    private Task createTask(String title, String desc, TaskStatus status,
                            TaskPriority priority, TaskType type,
                            LocalDate dueDate, Project project,
                            AppUser reporter, AppUser assignee) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(desc);
        t.setStatus(status);
        t.setPriority(priority);
        t.setTaskType(type);
        t.setDueDate(dueDate);
        t.setProject(project);
        t.setReporter(reporter);
        t.setAssignee(assignee);
        return taskRepo.save(t);
    }

    private void createComment(Task task, AppUser author, String content) {
        Comment c = new Comment();
        c.setContent(content);
        c.setTask(task);
        c.setAuthor(author);
        commentRepo.save(c);
    }

    private void createChecklist(Task task, String title, boolean checked) {
        ChecklistItem item = new ChecklistItem();
        item.setTitle(title);
        item.setChecked(checked);
        item.setTask(task);
        checklistRepo.save(item);
    }
}