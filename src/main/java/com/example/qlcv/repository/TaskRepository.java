package com.example.qlcv.repository;

import com.example.qlcv.entity.Task;
import com.example.qlcv.enums.TaskPriority;
import com.example.qlcv.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    // === Cũ: owner only ===
    Optional<Task> findByIdAndProjectOwnerEmail(Long id, String ownerEmail);
    long countByProjectOwnerEmail(String ownerEmail);
    long countByProjectOwnerEmailAndStatus(String ownerEmail, TaskStatus status);
    long countByProjectOwnerEmailAndPriority(String ownerEmail, TaskPriority priority);

    // === JIRA MINI: owner HOẶC member ===
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.project.owner.email = :email OR m.email = :email " +
           "ORDER BY t.createdAt DESC")
    List<Task> findAllAccessible(@Param("email") String email);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.id = :id AND (t.project.owner.email = :email OR m.email = :email)")
    Optional<Task> findAccessibleById(@Param("id") Long id, @Param("email") String email);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.project.id = :projectId AND (t.project.owner.email = :email OR m.email = :email) " +
           "ORDER BY t.createdAt DESC")
    List<Task> findAccessibleByProjectId(@Param("projectId") Long projectId, @Param("email") String email);

    @Query("SELECT COUNT(DISTINCT t) FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.project.owner.email = :email OR m.email = :email")
    long countAccessible(@Param("email") String email);

    @Query("SELECT COUNT(DISTINCT t) FROM Task t LEFT JOIN t.project.members m " +
           "WHERE (t.project.owner.email = :email OR m.email = :email) " +
           "AND t.priority = :priority")
    long countAccessibleByPriority(@Param("email") String email, @Param("priority") TaskPriority priority);

    @Query("SELECT COUNT(DISTINCT t) FROM Task t LEFT JOIN t.project.members m " +
           "WHERE (t.project.owner.email = :email OR m.email = :email) " +
           "AND t.dueDate IS NOT NULL AND t.dueDate < :today " +
           "AND t.status <> com.example.qlcv.enums.TaskStatus.DONE")
    long countAccessibleOverdue(@Param("email") String email, @Param("today") LocalDate today);

    @Query("SELECT t.status, COUNT(DISTINCT t) FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.project.owner.email = :email OR m.email = :email " +
           "GROUP BY t.status")
    List<Object[]> countAccessibleByStatus(@Param("email") String email);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN t.project.members m " +
           "WHERE t.project.owner.email = :email OR m.email = :email " +
           "ORDER BY t.createdAt DESC")
    List<Task> findAccessibleRecentTasks(@Param("email") String email);

    // === Tìm theo assignee ===
    List<Task> findByAssigneeEmailOrderByCreatedAtDesc(String email);
}