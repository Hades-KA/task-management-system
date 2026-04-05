package com.example.qlcv.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, STATUS_CHANGE

    @Column(nullable = false)
    private String entityType; // TASK, PROJECT

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String entityTitle;

    @Column(columnDefinition = "TEXT")
    private String detail; // "Status: TODO → IN_PROGRESS"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityTitle() { return entityTitle; }
    public void setEntityTitle(String entityTitle) { this.entityTitle = entityTitle; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // === Helper cho icon/color ===
        public String getActionIcon() {
        return switch (action) {
            case "CREATE" -> "ti ti-plus";
            case "UPDATE" -> "ti ti-edit";
            case "DELETE" -> "ti ti-trash";
            case "STATUS_CHANGE" -> "ti ti-arrows-exchange";
            case "COMMENT" -> "ti ti-message";
            case "ASSIGN" -> "ti ti-user-check";
            case "MEMBER_ADD" -> "ti ti-user-plus";
            case "MEMBER_REMOVE" -> "ti ti-user-minus";
            default -> "ti ti-activity";
        };
    }

    public String getActionColor() {
        return switch (action) {
            case "CREATE" -> "#22c55e";
            case "UPDATE" -> "#3b82f6";
            case "DELETE" -> "#fb7185";
            case "STATUS_CHANGE" -> "#fbbf24";
            case "COMMENT" -> "#a78bfa";
            case "ASSIGN" -> "#1fd1b9";
            case "MEMBER_ADD" -> "#22c55e";
            case "MEMBER_REMOVE" -> "#fb7185";
            default -> "#94a3b8";
        };
    }

    public String getActionLabel() {
        return switch (action) {
            case "CREATE" -> "Tạo mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "STATUS_CHANGE" -> "Đổi trạng thái";
            case "COMMENT" -> "Bình luận";
            case "ASSIGN" -> "Giao việc";
            case "MEMBER_ADD" -> "Thêm thành viên";
            case "MEMBER_REMOVE" -> "Xóa thành viên";
            default -> action;
        };
    }
}