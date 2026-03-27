package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Enrollment;
import com.university.system.model.User;
import com.university.system.repository.CourseRepository;
import com.university.system.repository.EnrollmentRepository;
import com.university.system.repository.UserRepository;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired private EnrollmentRepository enrollmentRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CourseService courseService;
    @Autowired private CourseRepository courseRepo;

    // Перегляд сторінки записів (для викладачів та адмінів)
    @GetMapping
    public String viewEnrollments(Model model, Principal principal) {
        User currentUser = userRepo.findByUsername(principal.getName());
        List<Enrollment> pending = new ArrayList<>();
        List<Enrollment> confirmed = new ArrayList<>();

        if (currentUser.getRole().equals("ADMIN")) {
            // Адмін бачить абсолютно всі заявки
            pending = enrollmentRepo.findAll().stream().filter(e -> !e.isConfirmed()).toList();
            confirmed = enrollmentRepo.findAll().stream().filter(e -> e.isConfirmed()).toList();
        } else if (currentUser.getRole().equals("TEACHER")) {
            // Викладач бачить заявки лише на свої курси
            List<Course> teacherCourses = courseRepo.findByTeacherId(currentUser.getId());
            for (Course course : teacherCourses) {
                pending.addAll(enrollmentRepo.findByCourseIdAndConfirmed(course.getId(), false));
                confirmed.addAll(enrollmentRepo.findByCourseIdAndConfirmed(course.getId(), true));
            }
        }

        model.addAttribute("pendingEnrollments", pending);
        model.addAttribute("confirmedEnrollments", confirmed);
        return "enroll";
    }

    // Студент подає заявку на курс
    @PostMapping("/apply")
    public String applyForCourse(@RequestParam Long courseId, Principal principal) {
        User student = userRepo.findByUsername(principal.getName());
        
        // Перевірка, чи вже є такий запис (щоб не дублювати)
        if (!enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(courseService.getCourseById(courseId).orElseThrow());
            enrollment.setConfirmed(false); // Очікує підтвердження
            enrollmentRepo.save(enrollment);
        }
        
        return "redirect:/student/dashboard";
    }

    // Викладач або Адмін підтверджує запис
    @PostMapping("/confirm/{id}")
    public String confirmEnrollment(@PathVariable Long id) {
        Enrollment enrollment = enrollmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment Id:" + id));
        enrollment.setConfirmed(true);
        enrollmentRepo.save(enrollment);
        
        return "redirect:/enrollments";
    }

    // Видалення запису (відхилення заявки або видалення студента з курсу)
    @PostMapping("/delete/{id}")
    public String deleteEnrollment(@PathVariable Long id) {
        enrollmentRepo.deleteById(id);
        return "redirect:/enrollments";
    }
}