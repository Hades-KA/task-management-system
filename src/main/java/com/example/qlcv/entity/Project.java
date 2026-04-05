package com.example.qlcv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên project không được để trống")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    // === JIRA MINI: Thành viên dự án ===
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> members = new HashSet<>();

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }

    public Set<AppUser> getMembers() { return members; }
    public void setMembers(Set<AppUser> members) { this.members = members; }

    // === Helper methods ===
    public void addMember(AppUser user) { this.members.add(user); }
    public void removeMember(AppUser user) { this.members.remove(user); }

    public boolean isMember(String email) {
        if (owner != null && owner.getEmail().equals(email)) return true;
        return members.stream().anyMatch(m -> m.getEmail().equals(email));
    }

    /** Trả về tất cả user có quyền (owner + members) */
    public Set<AppUser> getAllParticipants() {
        Set<AppUser> all = new HashSet<>(members);
        if (owner != null) all.add(owner);
        return all;
    }
}