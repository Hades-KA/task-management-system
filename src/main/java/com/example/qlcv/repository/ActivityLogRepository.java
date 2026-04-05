package com.example.qlcv.repository;

import com.example.qlcv.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserEmailOrderByCreatedAtDesc(String email);
    List<ActivityLog> findTop50ByUserEmailOrderByCreatedAtDesc(String email);
}