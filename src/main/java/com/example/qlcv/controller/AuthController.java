package com.example.qlcv.controller;

import com.example.qlcv.dto.RegisterForm;
import com.example.qlcv.entity.AppUser;
import com.example.qlcv.enums.Role;
import com.example.qlcv.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String registerForm(org.springframework.ui.Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult br) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "confirmPassword", "Mật khẩu nhập lại không khớp");
        }
        if (userRepo.existsByEmail(form.getEmail())) {
            br.rejectValue("email", "email", "Email đã tồn tại");
        }
        if (br.hasErrors()) return "auth/register";

        AppUser u = new AppUser();
        u.setFullName(form.getFullName());
        u.setEmail(form.getEmail());
        u.setPasswordHash(encoder.encode(form.getPassword()));
        u.setRole(Role.USER);
        u.setEnabled(true);

        userRepo.save(u);
        return "redirect:/login?registered";
    }
}