package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Enrollment;
import com.university.system.model.User;
import com.university.system.repository.EnrollmentRepository;
import com.university.system.repository.UserRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {
    @Autowired private CourseService courseService;
    @Autowired private EnrollmentRepository enrollmentRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ResultRepository resultRepo; // To show scores

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User student = userRepo.findByUsername(principal.getName());
        model.addAttribute("student", student);

        // Enrolled courses
        List<Enrollment> enrollments = enrollmentRepo.findByStudentId(student.getId());
        model.addAttribute("enrollments", enrollments);

        // Available courses (not yet enrolled)
        List<Long> enrolledCourseIds = enrollments.stream()
                                                    .map(e -> e.getCourse().getId())
                                                    .collect(Collectors.toList());
        
        List<com.university.system.model.Course> availableCourses = courseService.getAllCourses().stream()
                .filter(c -> !enrolledCourseIds.contains(c.getId()))
                .collect(Collectors.toList());
        model.addAttribute("availableCourses", availableCourses);
        
        // Scores for each enrolled course
        model.addAttribute("results", resultRepo.findByStudentId(student.getId()));

        return "student";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam Long courseId, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        if (!enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            Enrollment e = new Enrollment();
            e.setStudent(student);
            e.setCourse(courseService.getCourseById(courseId).orElse(null));
            enrollmentRepo.save(e);
        }
        return "redirect:/student/dashboard";
    }

    // New: View specific course for student (similar to CourseController, but might have student-specific info)
    @GetMapping("/course/{courseId}")
    public String viewStudentCourse(@PathVariable Long courseId, Model model, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        Course course = courseService.getCourseById(courseId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        
        // Ensure student is enrolled in this course
        if (!enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            return "redirect:/student/dashboard?error=not_enrolled";
        }

        model.addAttribute("course", course);
        model.addAttribute("lectures", course.getLectures()); // Assuming LAZY loading is configured correctly or fetched
        model.addAttribute("tests", course.getTests());       // Assuming LAZY loading is configured correctly or fetched
        model.addAttribute("student", student);
        model.addAttribute("results", resultRepo.findByStudentIdAndTestCourseId(student.getId(), courseId));

        return "course"; // Reusing the general course view
    }
}