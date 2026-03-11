package com.university.lms.controllers;

import com.university.lms.services.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final CourseService courseService;

    public StudentController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Отримуємо курси студента через Service
        model.addAttribute("username", userDetails.getUsername());
        // model.addAttribute("activeCourses", courseService.getActiveCourses(userDetails.getUsername()));
        return "student_dashboard"; // Повертає student_dashboard.html
    }
}