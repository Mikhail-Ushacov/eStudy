package com.university.system.controller;

import com.university.system.model.*;
import com.university.system.repository.*;
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

    private void checkCourseOwnership(Long courseId, Principal principal) {
        User teacher = userRepository.findByUsername(principal.getName());
        Course course = courseService.getCourseById(courseId).orElseThrow();
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Unauthorized operation on course: " + courseId);
        }
    }

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
        model.addAttribute("isAdmin", false);
        return "add-course";
    }

    @PostMapping("/course/add")
    public String addCourse(@ModelAttribute Course course, Principal principal) {
        User teacher = userRepository.findByUsername(principal.getName());
        course.setTeacher(teacher);
        courseService.saveCourse(course);
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        checkCourseOwnership(id, principal);
        courseService.deleteCourse(id);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/course/{courseId}")
    public String viewTeacherCourse(@PathVariable Long courseId, Model model, Principal principal) {
        checkCourseOwnership(courseId, principal);
        Course course = courseService.getCourseById(courseId).orElseThrow();
        
        model.addAttribute("enrollments", enrollmentRepository.findByCourseIdAndConfirmed(courseId, true));
        model.addAttribute("pendingRequests", enrollmentRepository.findByCourseIdAndConfirmed(courseId, false));
        
        List<Result> courseResults = resultRepository.findAll().stream()
                .filter(r -> r.getTest().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
        model.addAttribute("courseTestResults", courseResults);
        
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureRepository.findByCourseId(courseId));
        model.addAttribute("tests", testRepository.findByCourseId(courseId));
        return "course";
    }

    // LECTURE MANAGEMENT
    @GetMapping("/course/{courseId}/lecture/add")
    public String showAddLectureForm(@PathVariable Long courseId, Model model, Principal principal) {
        checkCourseOwnership(courseId, principal);
        Lecture lecture = new Lecture();
        lecture.setCourse(courseService.getCourseById(courseId).orElseThrow());
        model.addAttribute("lecture", lecture);
        return "add-lecture";
    }

    @PostMapping("/course/{courseId}/lecture/add")
    public String addLecture(@PathVariable Long courseId, @ModelAttribute Lecture lecture, Principal principal) {
        checkCourseOwnership(courseId, principal);
        lecture.setCourse(courseService.getCourseById(courseId).orElseThrow());
        lectureRepository.save(lecture);
        return "redirect:/teacher/course/" + courseId;
    }

    @PostMapping("/lecture/delete/{id}")
    public String deleteLecture(@PathVariable Long id, Principal principal) {
        Lecture lecture = lectureRepository.findById(id).orElseThrow();
        checkCourseOwnership(lecture.getCourse().getId(), principal);
        lectureRepository.deleteById(id);
        return "redirect:/teacher/course/" + lecture.getCourse().getId();
    }

    // TEST MANAGEMENT
    @GetMapping("/course/{courseId}/test/add")
    public String showAddTestForm(@PathVariable Long courseId, Model model, Principal principal) {
        checkCourseOwnership(courseId, principal);
        Test test = new Test();
        test.setCourse(courseService.getCourseById(courseId).orElseThrow());
        model.addAttribute("test", test);
        return "add-test";
    }

    @PostMapping("/course/{courseId}/test/add")
    public String addTest(@PathVariable Long courseId, @ModelAttribute Test test, Principal principal) {
        checkCourseOwnership(courseId, principal);
        test.setCourse(courseService.getCourseById(courseId).orElseThrow());
        
        // Обов'язково зв'язуємо об'єкти між собою для JPA
        if (test.getQuestions() != null) {
            test.getQuestions().forEach(q -> {
                q.setTest(test);
                if (q.getOptions() != null) {
                    q.getOptions().forEach(opt -> opt.setQuestion(q));
                }
            });
        }
        
        testRepository.save(test);
        return "redirect:/teacher/course/" + courseId;
    }

    @PostMapping("/test/delete/{testId}")
    public String deleteTest(@PathVariable Long testId, Principal principal) {
        Test test = testRepository.findById(testId).orElseThrow();
        checkCourseOwnership(test.getCourse().getId(), principal);
        testRepository.deleteById(testId);
        return "redirect:/teacher/course/" + test.getCourse().getId();
    }

    // QUESTION MANAGEMENT
    @GetMapping("/test/{testId}/question/add")
    public String showAddQuestionForm(@PathVariable Long testId, Model model, Principal principal) {
        Test test = testRepository.findById(testId).orElseThrow();
        checkCourseOwnership(test.getCourse().getId(), principal);
        model.addAttribute("question", new Question());
        model.addAttribute("test", test);
        return "add-question";
    }

    @PostMapping("/test/{testId}/question/add")
    public String addQuestion(@PathVariable Long testId, @ModelAttribute Question question, Principal principal) {
        Test test = testRepository.findById(testId).orElseThrow();
        checkCourseOwnership(test.getCourse().getId(), principal);
        question.setTest(test);
        questionRepository.save(question); 
        return "redirect:/teacher/course/" + test.getCourse().getId();
    }

    @PostMapping("/enroll/confirm/{enrollmentId}")
    public String confirmEnrollment(@PathVariable Long enrollmentId, Principal principal) {
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElseThrow();
        checkCourseOwnership(e.getCourse().getId(), principal);
        e.setConfirmed(true);
        enrollmentRepository.save(e);
        return "redirect:/teacher/course/" + e.getCourse().getId();
    }
}