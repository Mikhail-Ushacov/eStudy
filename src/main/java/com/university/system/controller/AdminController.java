package com.university.system.controller;

import com.university.system.model.User;
import com.university.system.repository.UserRepository;
import com.university.system.service.CourseService;
// import com.university.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    // @Autowired private UserService userService;
    @Autowired private CourseService courseService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/user/update-role")
    public String updateRole(@RequestParam Long userId, @RequestParam String role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }
}