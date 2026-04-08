package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Enrollment;
import com.university.system.model.User;
import com.university.system.repository.EnrollmentRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.repository.UserRepository;
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
    @Autowired private ResultRepository resultRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        model.addAttribute("student", student);

        model.addAttribute("enrollments", enrollmentRepo.findByStudentIdAndConfirmed(student.getId(), true));
        model.addAttribute("pendingEnrollments", enrollmentRepo.findByStudentIdAndConfirmed(student.getId(), false));

        List<Long> appliedCourseIds = enrollmentRepo.findByStudentId(student.getId()).stream()
                .map(e -> e.getCourse().getId()).collect(Collectors.toList());
        
        List<Course> availableCourses = courseService.getAllCourses().stream()
                .filter(c -> !appliedCourseIds.contains(c.getId()))
                .collect(Collectors.toList());
        
        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("results", resultRepo.findByStudentIdWithTestAndCourse(student.getId()));
        
        return "student";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam Long courseId, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        if (enrollmentRepo.findByStudentIdAndCourseId(student.getId(), courseId) == null) {
            Enrollment e = new Enrollment();
            e.setStudent(student);
            e.setCourse(courseService.getCourseById(courseId).orElseThrow());
            e.setConfirmed(false);
            enrollmentRepo.save(e);
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/course/{courseId}")
    public String viewStudentCourse(@PathVariable Long courseId, Model model, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        Enrollment enrollment = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), courseId);

        if (enrollment == null || !enrollment.isConfirmed()) {
            return "redirect:/student/dashboard?error=access_denied";
        }

        Course course = enrollment.getCourse();
        model.addAttribute("course", course);
        model.addAttribute("lectures", course.getLectures());
        model.addAttribute("tests", course.getTests());
        
        List<Long> completedTestIds = resultRepo.findByStudentIdWithTestAndCourse(student.getId()).stream()
                .map(r -> r.getTest().getId()).collect(Collectors.toList());
        model.addAttribute("completedTestIds", completedTestIds);

        return "course";
    }
}