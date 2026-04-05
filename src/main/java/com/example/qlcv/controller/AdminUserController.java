package com.example.qlcv.controller;

import com.example.qlcv.enums.Role;
import com.example.qlcv.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepo;
    public AdminUserController(UserRepository userRepo) { this.userRepo = userRepo; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        var u = userRepo.findById(id).orElseThrow();
        u.setEnabled(!u.isEnabled());
        userRepo.save(u);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role) {
        var u = userRepo.findById(id).orElseThrow();
        u.setRole(role);
        userRepo.save(u);
        return "redirect:/admin/users";
    }
}