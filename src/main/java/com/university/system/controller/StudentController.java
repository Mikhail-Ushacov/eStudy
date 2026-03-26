package com.university.system.controller;

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
@RequestMapping("/student")
public class StudentController {
    @Autowired private CourseService courseService;
    @Autowired private EnrollmentRepository enrollmentRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User student = userRepo.findByUsername(principal.getName());
        model.addAttribute("enrollments", enrollmentRepo.findByStudentId(student.getId()));
        model.addAttribute("availableCourses", courseService.getAllCourses());
        
        return "student";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam Long courseId, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        if (!enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            Enrollment e = new Enrollment();
            e.setStudent(student);
            e.setCourse(courseService.getAllCourses().stream()
                .filter(c -> c.getId().equals(courseId)).findFirst().orElse(null));
            enrollmentRepo.save(e);
        }
        return "redirect:/student/dashboard";
    }
}