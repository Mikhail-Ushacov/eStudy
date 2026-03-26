package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.User;
import com.university.system.repository.UserRepository;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired private CourseService courseService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin";
    }

    // User management
    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/user/update-role")
    public String updateRole(@RequestParam Long userId, @RequestParam String role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        user.setRole(role.replace("ROLE_", "")); // Store role without "ROLE_" prefix
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    // Course management for Admin
    @GetMapping("/course/add")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("teachers", userRepository.findByRole("TEACHER"));
        return "add-course";
    }

    @PostMapping("/course/add")
    public String addCourse(@ModelAttribute Course course, @RequestParam Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + teacherId));
        course.setTeacher(teacher);
        courseService.saveCourse(course);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/course/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        model.addAttribute("course", course);
        model.addAttribute("teachers", userRepository.findByRole("TEACHER"));
        return "add-course"; // Reusing add-course form for editing
    }

    @PostMapping("/course/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, @RequestParam Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + teacherId));
        course.setId(id); // Ensure the ID is set for update
        course.setTeacher(teacher);
        courseService.saveCourse(course);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin/dashboard";
    }
}