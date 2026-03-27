package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Enrollment;
import com.university.system.model.Lecture;
import com.university.system.model.Test;
import com.university.system.model.Question;
import com.university.system.model.User;
import com.university.system.repository.CourseRepository;
import com.university.system.repository.EnrollmentRepository;
import com.university.system.repository.LectureRepository;
import com.university.system.repository.QuestionRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.repository.TestRepository;
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
@RequestMapping("/teacher")
public class TeacherController {
    @Autowired private CourseService courseService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LectureRepository lectureRepository;
    @Autowired private TestRepository testRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ResultRepository resultRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User teacher = userRepository.findByUsername(principal.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courseRepository.findByTeacherId(teacher.getId()));
        return "teacher";
    }

    @GetMapping("/course/add")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "add-course";
    }

    @PostMapping("/course/add")
    public String addCourse(@ModelAttribute Course course, Principal principal) {
        User teacher = userRepository.findByUsername(principal.getName());
        course.setTeacher(teacher);
        courseService.saveCourse(course);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/course/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        model.addAttribute("course", course);
        return "add-course"; // Reusing add-course form for editing
    }

    @PostMapping("/course/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, Principal principal) {
        User teacher = userRepository.findByUsername(principal.getName());
        course.setId(id); // Ensure the ID is set for update
        course.setTeacher(teacher);
        courseService.saveCourse(course);
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/course/{courseId}")
    public String viewTeacherCourse(@PathVariable Long courseId, Model model, Principal principal) {
        Course course = courseService.getCourseById(courseId).orElseThrow();
        
        // Студенти, які вже на курсі
        model.addAttribute("enrollments", enrollmentRepository.findByCourseIdAndConfirmed(courseId, true));
        
        // ЗАЯВКИ на підтвердження
        model.addAttribute("pendingRequests", enrollmentRepository.findByCourseIdAndConfirmed(courseId, false));
        
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureRepository.findByCourseId(courseId));
        model.addAttribute("tests", testRepository.findByCourseId(courseId));
        return "course";
    }

    @PostMapping("/enroll/confirm/{enrollmentId}")
    public String confirmEnrollment(@PathVariable Long enrollmentId) {
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElseThrow();
        e.setConfirmed(true);
        enrollmentRepository.save(e);
        return "redirect:/teacher/course/" + e.getCourse().getId();
    }

    // Methods to manage tests and questions (new or moved from TestController for teacher role)
    @GetMapping("/course/{courseId}/test/add")
    public String showAddTestForm(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        Test test = new Test();
        test.setCourse(course);
        model.addAttribute("test", test);
        return "add-test";
    }

    @PostMapping("/course/{courseId}/test/add")
    public String addTest(@PathVariable Long courseId, @ModelAttribute Test test) {
        Course course = courseService.getCourseById(courseId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        test.setCourse(course);
        testRepository.save(test);
        return "redirect:/teacher/course/" + courseId;
    }

    @PostMapping("/test/delete/{testId}")
    public String deleteTest(@PathVariable Long testId) {
        Test test = testRepository.findById(testId)
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid test Id:" + testId));
        Long courseId = test.getCourse().getId();
        testRepository.deleteById(testId);
        return "redirect:/teacher/course/" + courseId;
    }

    // Метод для відображення форми (рядок ~147)
    @GetMapping("/test/{testId}/question/add")
    public String showAddQuestionForm(@PathVariable Long testId, Model model) {
        // ВИПРАВЛЕНО: використовуємо назву testRepository
        Test test = testRepository.findById(testId)
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid test Id:" + testId));
        model.addAttribute("question", new com.university.system.model.Question());
        model.addAttribute("test", test);
        return "add-question";
    }

    // Метод для збереження питання (рядок ~154)
    @PostMapping("/test/{testId}/question/add")
    public String addQuestion(@PathVariable Long testId, @ModelAttribute com.university.system.model.Question question) {
        // ВИПРАВЛЕНО: використовуємо назву testRepository замість testRepo
        Test test = testRepository.findById(testId)
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid test Id:" + testId));
        
        question.setTest(test);
        
        // ВИПРАВЛЕНО: використовуємо questionRepository замість lectureRepository
        questionRepository.save(question); 
        
        return "redirect:/teacher/course/" + test.getCourse().getId();
    }
}