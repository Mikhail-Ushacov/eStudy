package com.university.system.controller;

import com.university.system.model.User;
import com.university.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ім'я користувача обов'язкове");
            return "redirect:/register";
        }

        if (username.length() < 3 || username.length() > 50) {
            redirectAttributes.addFlashAttribute("error", "Ім'я користувача має бути від 3 до 50 символів");
            return "redirect:/register";
        }

        if (password == null || password.length() < 1) {
            redirectAttributes.addFlashAttribute("error", "Пароль обов'язковий");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Паролі не збігаються");
            return "redirect:/register";
        }

        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("error", "Ім'я користувача вже існує");
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("STUDENT");

        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("success", "Реєстрація успішна! Будь ласка, увійдіть.");
        return "redirect:/login";
    }
}