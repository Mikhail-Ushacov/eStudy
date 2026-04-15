package com.university.system.controller;

import com.university.system.model.*;
import com.university.system.repository.*;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("lectures", lectureRepository.findAll());
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("questions", questionRepository.findAll());
        model.addAttribute("results", resultRepository.findAll());
        model.addAttribute("enrollments", enrollmentRepository.findAll());
        
        // Списки для випадаючих меню у формах швидкого додавання
        model.addAttribute("teachers", userRepository.findByRole("TEACHER"));
        model.addAttribute("students", userRepository.findByRole("STUDENT"));
        return "admin";
    }

    // --- УПРАВЛІННЯ КОРИСТУВАЧАМИ ---

    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        // Якщо це новий користувач або пароль було змінено (не зашифрований)
        if (user.getId() == null || (user.getPassword() != null && user.getPassword().length() < 30)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/user/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/user/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, @RequestParam(required = false) String newPassword) {
        User existingUser = userRepository.findById(id).orElseThrow();
        existingUser.setUsername(user.getUsername());
        existingUser.setRole(user.getRole());
        
        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        userRepository.save(existingUser);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- УПРАВЛІННЯ КУРСАМИ ---

    @GetMapping("/course/add")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("teachers", userRepository.findByRole("TEACHER"));
        model.addAttribute("isAdmin", true);
        return "add-course";
    }

    @PostMapping("/course/save")
    public String saveCourse(@ModelAttribute Course course, @RequestParam Long teacherId) {
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

    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin/dashboard";
    }

    // --- УПРАВЛІННЯ ЛЕКЦІЯМИ ---

    @GetMapping("/lecture/edit/{id}")
    public String showEditLectureForm(@PathVariable Long id, Model model) {
        Lecture lecture = lectureRepository.findById(id).orElseThrow();
        model.addAttribute("lecture", lecture);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("isAdmin", true);
        return "add-lecture";
    }

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

    // --- УПРАВЛІННЯ ТЕСТАМИ ---

    @GetMapping("/test/edit/{id}")
    public String showEditTestForm(@PathVariable Long id, Model model) {
        Test test = testRepository.findById(id).orElseThrow();
        model.addAttribute("test", test);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("isAdmin", true);
        return "add-test";
    }

    @PostMapping("/test/save")
    public String saveTest(@ModelAttribute Test test, 
                        @RequestParam(required = false) Long courseId, 
                        @RequestParam(required = false) Boolean isFinal) {
        
        if (courseId != null) {
            test.setCourse(courseService.getCourseById(courseId).orElseThrow());
        }
        
        // Встановлюємо статус: якщо checkbox не відмічений, isFinal буде null або false
        boolean finalStatus = (isFinal != null && isFinal);
        test.setFinalTest(finalStatus);
        
        // --- ЛОГІКА ПЕРЕВІРКИ: Тільки один фінальний тест на курс ---
        if (finalStatus && test.getCourse() != null) {
            // Шукаємо всі тести цього курсу, які вже позначені як фінальні
            List<Test> existingFinals = testRepository.findByCourseIdAndFinalTest(test.getCourse().getId(), true);
            
            for (Test t : existingFinals) {
                // Якщо ми створюємо новий тест або редагуємо інший — знімаємо прапорець зі старого
                if (test.getId() == null || !t.getId().equals(test.getId())) {
                    t.setFinalTest(false);
                    testRepository.save(t);
                }
            }
        }
        // ---------------------------------------------------------
        
        if (test.getQuestions() != null) {
            test.getQuestions().removeIf(q -> q.getQuestion() == null || q.getQuestion().trim().isEmpty());
            
            for (Question q : test.getQuestions()) {
                q.setTest(test);
                if (q.getOptions() != null) {
                    for (AnswerOption opt : q.getOptions()) {
                        opt.setQuestion(q);
                    }
                }
            }
        }
        
        testRepository.save(test);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/test/delete/{id}")
    public String deleteTest(@PathVariable Long id) {
        testRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- УПРАВЛІННЯ ПИТАННЯМИ ---

    @GetMapping("/test/{testId}/question/add")
    public String showAddQuestionForm(@PathVariable Long testId, Model model) {
        Test test = testRepository.findById(testId).orElseThrow();
        model.addAttribute("question", new Question());
        model.addAttribute("test", test);
        model.addAttribute("isAdmin", true);
        return "add-question";
    }

    @GetMapping("/question/edit/{id}")
    public String showEditQuestionForm(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id).orElseThrow();
        model.addAttribute("question", question);
        model.addAttribute("test", question.getTest());
        model.addAttribute("isAdmin", true);
        return "add-question";
    }

    @PostMapping("/question/save")
    public String saveQuestion(@ModelAttribute Question question, @RequestParam Long testId) {
        Test test = testRepository.findById(testId).orElseThrow();
        question.setTest(test);
        
        if (question.getOptionA() != null && !question.getOptionA().isEmpty()) {
            AnswerOption opt = new AnswerOption();
            opt.setOptionText(question.getOptionA());
            opt.setCorrect("A".equals(question.getCorrectAnswer()));
            opt.setQuestion(question);
            question.getOptions().add(opt);
        }
        if (question.getOptionB() != null && !question.getOptionB().isEmpty()) {
            AnswerOption opt = new AnswerOption();
            opt.setOptionText(question.getOptionB());
            opt.setCorrect("B".equals(question.getCorrectAnswer()));
            opt.setQuestion(question);
            question.getOptions().add(opt);
        }
        if (question.getOptionC() != null && !question.getOptionC().isEmpty()) {
            AnswerOption opt = new AnswerOption();
            opt.setOptionText(question.getOptionC());
            opt.setCorrect("C".equals(question.getCorrectAnswer()));
            opt.setQuestion(question);
            question.getOptions().add(opt);
        }
        if (question.getOptionD() != null && !question.getOptionD().isEmpty()) {
            AnswerOption opt = new AnswerOption();
            opt.setOptionText(question.getOptionD());
            opt.setCorrect("D".equals(question.getCorrectAnswer()));
            opt.setQuestion(question);
            question.getOptions().add(opt);
        }
        
        questionRepository.save(question);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/question/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- УПРАВЛІННЯ ЗАПИСАМИ (ENROLLMENTS) ---

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

    // --- УПРАВЛІННЯ РЕЗУЛЬТАТАМИ ---

    @PostMapping("/result/delete/{id}")
    public String deleteResult(@PathVariable Long id) {
        resultRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // Додайте цей метод у AdminController.java

    @PostMapping("/test/{testId}/set-final")
    @ResponseBody
    public String setFinalTest(@PathVariable Long testId, @RequestParam boolean isFinal) {
        Test currentTest = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid test Id:" + testId));

        if (isFinal && currentTest.getCourse() != null) {
            // Знаходимо всі фінальні тести цього курсу та знімаємо позначку (може бути лише один)
            List<Test> finalTests = testRepository.findByCourseIdAndFinalTest(currentTest.getCourse().getId(), true);
            for (Test t : finalTests) {
                t.setFinalTest(false);
                testRepository.save(t);
            }
        }

        currentTest.setFinalTest(isFinal);
        testRepository.save(currentTest);

        return "Оновлено";
    }
}