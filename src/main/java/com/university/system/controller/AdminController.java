package com.university.system.controller;

import com.university.system.model.*;
import com.university.system.repository.*;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired private CourseService courseService;
    @Autowired private UserRepository userRepository;
    @Autowired private LectureRepository lectureRepository;
    @Autowired private TestRepository testRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ResultRepository resultRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("lectures", lectureRepository.findAll());
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("questions", questionRepository.findAll());
        model.addAttribute("results", resultRepository.findAll());
        model.addAttribute("enrollments", enrollmentRepository.findAll());
        
        // Для форм додавання
        model.addAttribute("teachers", userRepository.findByRole("TEACHER"));
        model.addAttribute("students", userRepository.findByRole("STUDENT"));
        return "admin";
    }

    // --- USERS ---
    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- COURSES ---
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
        model.addAttribute("isAdmin", true);
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
        model.addAttribute("isAdmin", true);
        return "add-course";
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

    // --- LECTURES ---
    @PostMapping("/lecture/save")
    public String saveLecture(@ModelAttribute Lecture lecture, @RequestParam Long courseId) {
        lecture.setCourse(courseService.getCourseById(courseId).orElseThrow());
        lectureRepository.save(lecture);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/lecture/delete/{id}")
    public String deleteLecture(@PathVariable Long id) {
        lectureRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- TESTS ---
    @PostMapping("/test/save")
    public String saveTest(@ModelAttribute Test test, @RequestParam Long courseId) {
        test.setCourse(courseService.getCourseById(courseId).orElseThrow());
        testRepository.save(test);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/test/delete/{id}")
    public String deleteTest(@PathVariable Long id) {
        testRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- QUESTIONS ---
    @PostMapping("/question/save")
    public String saveQuestion(@ModelAttribute Question question, @RequestParam Long testId) {
        question.setTest(testRepository.findById(testId).orElseThrow());
        questionRepository.save(question);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/question/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- ENROLLMENTS ---
    @PostMapping("/enrollment/confirm/{id}")
    public String confirmEnroll(@PathVariable Long id) {
        Enrollment e = enrollmentRepository.findById(id).orElseThrow();
        e.setConfirmed(true);
        enrollmentRepository.save(e);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/enrollment/delete/{id}")
    public String deleteEnroll(@PathVariable Long id) {
        enrollmentRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- RESULTS ---
    @PostMapping("/result/delete/{id}")
    public String deleteResult(@PathVariable Long id) {
        resultRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}