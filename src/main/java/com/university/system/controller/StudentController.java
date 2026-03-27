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
        
        // ВАЖЛИВО: Додаємо самого студента в модель, щоб працював ${student.username}
        model.addAttribute("student", student);

        // Курси, на які студент вже ПІДТВЕРДЖЕНИЙ
        List<Enrollment> myConfirmedEnrollments = enrollmentRepo.findByStudentIdAndConfirmed(student.getId(), true);
        model.addAttribute("enrollments", myConfirmedEnrollments);

        // Заявки, які ще чекають на підтвердження
        List<Enrollment> pendingEnrollments = enrollmentRepo.findByStudentIdAndConfirmed(student.getId(), false);
        model.addAttribute("pendingEnrollments", pendingEnrollments);

        // Доступні курси (на які ще немає жодної заявки)
        List<Long> allAppliedCourseIds = enrollmentRepo.findByStudentId(student.getId()).stream()
                .map(e -> e.getCourse().getId()).collect(Collectors.toList());
        
        List<Course> availableCourses = courseService.getAllCourses().stream()
                .filter(c -> !allAppliedCourseIds.contains(c.getId()))
                .collect(Collectors.toList());
        
        model.addAttribute("availableCourses", availableCourses);
        
        // Результати тестів
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
            e.setConfirmed(false); // За замовчуванням не підтверджено
            enrollmentRepo.save(e);
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/course/{courseId}")
    public String viewStudentCourse(@PathVariable Long courseId, Model model, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        // Перевіряємо, чи студент не просто подав заявку, а чи вона ПІДТВЕРДЖЕНА
        Enrollment enrollment = enrollmentRepo.findByStudentIdAndConfirmed(student.getId(), true).stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (enrollment == null) {
            return "redirect:/student/dashboard?error=access_denied";
        }

        Course course = enrollment.getCourse();
        model.addAttribute("course", course);
        model.addAttribute("lectures", course.getLectures());
        model.addAttribute("tests", course.getTests());
        return "course";
    }
}