package com.example.qlcv.repository;

import com.example.qlcv.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // === Cũ: chỉ owner ===
    List<Project> findByOwnerEmailOrderByCreatedAtDesc(String email);
    Optional<Project> findByIdAndOwnerEmail(Long id, String email);
    long countByOwnerEmail(String email);

    // === JIRA MINI: owner HOẶC member ===
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
           "WHERE p.owner.email = :email OR m.email = :email " +
           "ORDER BY p.createdAt DESC")
    List<Project> findAllAccessible(@Param("email") String email);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m " +
           "WHERE p.id = :id AND (p.owner.email = :email OR m.email = :email)")
    Optional<Project> findAccessibleById(@Param("id") Long id, @Param("email") String email);

    @Query("SELECT COUNT(DISTINCT p) FROM Project p LEFT JOIN p.members m " +
           "WHERE p.owner.email = :email OR m.email = :email")
    long countAccessible(@Param("email") String email);
}