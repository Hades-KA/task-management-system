package com.example.qlcv.repository;

import com.example.qlcv.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    long countByTaskId(Long taskId);
    long countByTaskIdAndChecked(Long taskId, boolean checked);
}