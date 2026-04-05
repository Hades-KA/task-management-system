package com.example.qlcv.service;

import com.example.qlcv.entity.AppUser;
import com.example.qlcv.entity.Comment;
import com.example.qlcv.entity.Task;
import com.example.qlcv.repository.CommentRepository;
import com.example.qlcv.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final TaskService taskService;
    private final ActivityLogService activityLogService;

    public CommentService(CommentRepository commentRepo, UserRepository userRepo,
                          TaskService taskService, ActivityLogService activityLogService) {
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.taskService = taskService;
        this.activityLogService = activityLogService;
    }

    public List<Comment> getByTask(Long taskId) {
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    public Comment addComment(Long taskId, String email, String content) {
        Task task = taskService.getAccessibleOrThrow(taskId, email);
        AppUser author = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        Comment c = new Comment();
        c.setContent(content);
        c.setTask(task);
        c.setAuthor(author);
        c = commentRepo.save(c);

        activityLogService.log(email, "COMMENT", "TASK",
                task.getId(), task.getTitle(),
                "Thêm bình luận: \"" + (content.length() > 50 ? content.substring(0, 50) + "..." : content) + "\"");

        return c;
    }

    public void deleteComment(Long commentId, String email) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bình luận"));

        // Chỉ tác giả mới được xóa
        if (!c.getAuthor().getEmail().equals(email)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa bình luận này");
        }

        commentRepo.delete(c);
    }

    public long countByTask(Long taskId) {
        return commentRepo.countByTaskId(taskId);
    }
}