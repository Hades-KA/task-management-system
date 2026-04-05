package com.example.qlcv.service;

import com.example.qlcv.entity.ActivityLog;
import com.example.qlcv.entity.AppUser;
import com.example.qlcv.repository.ActivityLogRepository;
import com.example.qlcv.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository logRepo;
    private final UserRepository userRepo;

    public ActivityLogService(ActivityLogRepository logRepo, UserRepository userRepo) {
        this.logRepo = logRepo;
        this.userRepo = userRepo;
    }

    public void log(String email, String action, String entityType,
                    Long entityId, String entityTitle, String detail) {
        AppUser user = userRepo.findByEmail(email).orElse(null);
        if (user == null) return;

        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityTitle(entityTitle);
        log.setDetail(detail);
        log.setUser(user);
        logRepo.save(log);
    }

    public List<ActivityLog> getRecentLogs(String email) {
        return logRepo.findTop50ByUserEmailOrderByCreatedAtDesc(email);
    }
}