package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Enrollment;
import com.university.system.model.User;
import com.university.system.repository.EnrollmentRepository;
import com.university.system.repository.UserRepository;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
public class CourseController {

    @Autowired private CourseService courseService;
    @Autowired private UserRepository userRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    @GetMapping({"/", "/courses"})
    public String getCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "home";
    }

    @GetMapping("/courses/{id}")
    public String viewCoursePublic(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.getCourseById(id)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));

        if (principal != null) {
            User user = userRepo.findByUsername(principal.getName());
            
            // Redirect teachers to their manage view
            if ("TEACHER".equals(user.getRole()) && course.getTeacher().getId().equals(user.getId())) {
                return "redirect:/teacher/course/" + id;
            }
            
            // Redirect students properly based on enrollment
            if ("STUDENT".equals(user.getRole())) {
                Enrollment e = enrollmentRepo.findByStudentIdAndCourseId(user.getId(), id);
                if (e != null && e.isConfirmed()) {
                    return "redirect:/student/course/" + id;
                }
                model.addAttribute("enrollment", e);
            }
        }

        model.addAttribute("course", course);
        return "course-info";
    }
}