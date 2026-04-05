package com.example.qlcv.service;

import com.example.qlcv.entity.AppUser;
import com.example.qlcv.entity.Project;
import com.example.qlcv.repository.ProjectRepository;
import com.example.qlcv.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLogService;

    public ProjectService(ProjectRepository projectRepo, UserRepository userRepo,
                          ActivityLogService activityLogService) {
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.activityLogService = activityLogService;
    }

    /** Lấy tất cả project mà user có quyền (owner hoặc member) */
    public List<Project> listForUser(String email) {
        return projectRepo.findAllAccessible(email);
    }

    /** Kiểm tra quyền truy cập project (owner hoặc member) */
    public Project getForUserOrThrow(Long id, String email) {
        return projectRepo.findAccessibleById(id, email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy project hoặc không có quyền"));
    }

    /** Kiểm tra user có phải owner không */
    public boolean isOwner(Long projectId, String email) {
        Project p = getForUserOrThrow(projectId, email);
        return p.getOwner().getEmail().equals(email);
    }

    /** Tạo project — người tạo là owner */
    public Project createForUser(Project form, String email) {
        AppUser owner = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        Project p = new Project();
        p.setName(form.getName());
        p.setDescription(form.getDescription());
        p.setOwner(owner);
        p = projectRepo.save(p);

        activityLogService.log(email, "CREATE", "PROJECT",
                p.getId(), p.getName(), "Tạo project mới");

        return p;
    }

    /** Sửa project — chỉ owner */
    public Project updateForUser(Long id, Project form, String email) {
        Project p = getForUserOrThrow(id, email);
        if (!p.getOwner().getEmail().equals(email)) {
            throw new IllegalArgumentException("Chỉ chủ sở hữu mới được sửa project");
        }

        String oldName = p.getName();
        p.setName(form.getName());
        p.setDescription(form.getDescription());
        p = projectRepo.save(p);

        String detail = oldName.equals(form.getName())
                ? "Cập nhật thông tin project"
                : "Đổi tên: \"" + oldName + "\" → \"" + form.getName() + "\"";
        activityLogService.log(email, "UPDATE", "PROJECT", p.getId(), p.getName(), detail);

        return p;
    }

    /** Xóa project — chỉ owner */
    public void deleteForUser(Long id, String email) {
        Project p = getForUserOrThrow(id, email);
        if (!p.getOwner().getEmail().equals(email)) {
            throw new IllegalArgumentException("Chỉ chủ sở hữu mới được xóa project");
        }

        String name = p.getName();
        projectRepo.delete(p);
        activityLogService.log(email, "DELETE", "PROJECT", id, name, "Xóa project");
    }

    /** Đếm project mà user có quyền */
    public long countProjects(String email) {
        return projectRepo.countAccessible(email);
    }

    // ========== QUẢN LÝ THÀNH VIÊN ==========

    /** Thêm thành viên vào project — chỉ owner */
    @Transactional
    public void addMember(Long projectId, String memberEmail, String ownerEmail) {
        Project p = getForUserOrThrow(projectId, ownerEmail);
        if (!p.getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("Chỉ chủ sở hữu mới được thêm thành viên");
        }

        AppUser member = userRepo.findByEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + memberEmail));

        if (member.getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("Owner đã là thành viên mặc định");
        }

        p.addMember(member);
        projectRepo.save(p);

        activityLogService.log(ownerEmail, "MEMBER_ADD", "PROJECT",
                p.getId(), p.getName(),
                "Thêm thành viên: " + member.getFullName() + " (" + member.getEmail() + ")");
    }

    /** Xóa thành viên khỏi project — chỉ owner */
    @Transactional
    public void removeMember(Long projectId, Long memberId, String ownerEmail) {
        Project p = getForUserOrThrow(projectId, ownerEmail);
        if (!p.getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("Chỉ chủ sở hữu mới được xóa thành viên");
        }

        AppUser member = userRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        p.removeMember(member);
        projectRepo.save(p);

        activityLogService.log(ownerEmail, "MEMBER_REMOVE", "PROJECT",
                p.getId(), p.getName(),
                "Xóa thành viên: " + member.getFullName());
    }

    /** Lấy danh sách tất cả participant (owner + members) */
    public Set<AppUser> getParticipants(Long projectId, String email) {
        Project p = getForUserOrThrow(projectId, email);
        return p.getAllParticipants();
    }
}