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
        
        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollmentRepository.findByCourseIdAndConfirmed(courseId, true));
        
        // Отримуємо результати всіх тестів цього курсу
        List<Result> courseResults = resultRepository.findAll().stream()
                .filter(r -> r.getTest().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
        model.addAttribute("courseTestResults", courseResults);
        
        model.addAttribute("lectures", lectureRepository.findByCourseId(courseId));
        model.addAttribute("tests", testRepository.findByCourseId(courseId));
        model.addAttribute("pendingRequests", enrollmentRepository.findByCourseIdAndConfirmed(courseId, false));
        
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
        
        if (lecture.getSections() != null) {
            for (int i = 0; i < lecture.getSections().size(); i++) {
                LectureSection section = lecture.getSections().get(i);
                section.setLecture(lecture);
                section.setOrderIndex(i);
            }
        }
        
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
        model.addAttribute("isAdmin", false); // <-- ДОДАЙТЕ ЦЕЙ РЯДОК
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

    @GetMapping("/test/edit/{id}")
    public String showEditTestForm(@PathVariable Long id, Model model, Principal principal) {
        Test test = testRepository.findById(id).orElseThrow();
        checkCourseOwnership(test.getCourse().getId(), principal);
        model.addAttribute("test", test);
        model.addAttribute("isAdmin", false);
        return "add-test";
    }

    @PostMapping("/test/{testId}/set-final")
    @ResponseBody // Повертаємо JSON/текст, а не сторінку
    public String setFinalTest(@PathVariable Long testId, @RequestParam boolean isFinal, Principal principal) {
        Test currentTest = testRepository.findById(testId).orElseThrow();
        checkCourseOwnership(currentTest.getCourse().getId(), principal);

        if (isFinal) {
            // Знаходимо всі тести цього курсу, які вже позначені як фінальні
            List<Test> finalTests = testRepository.findByCourseIdAndFinalTest(currentTest.getCourse().getId(), true);
            
            // Знімаємо позначку з усіх інших
            for (Test t : finalTests) {
                t.setFinalTest(false);
                testRepository.save(t);
            }
        }

        currentTest.setFinalTest(isFinal);
        testRepository.save(currentTest);

        return "Оновлено";
    }

    @GetMapping("/course/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model, Principal principal) {
        // Перевіряємо, чи належить курс цьому викладачу
        checkCourseOwnership(id, principal);
        
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        
        model.addAttribute("course", course);
        model.addAttribute("isAdmin", false); // Кажемо формі, що ми НЕ адмін
        return "add-course"; // Використовуємо ту саму форму, що й для додавання
    }

    @PostMapping("/course/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, Principal principal) {
        // Перевіряємо права власності
        checkCourseOwnership(id, principal);
        
        Course existingCourse = courseService.getCourseById(id).orElseThrow();
        
        // Оновлюємо лише дозволені поля
        existingCourse.setName(course.getName());
        existingCourse.setDescription(course.getDescription());
        
        courseService.saveCourse(existingCourse);
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/lecture/edit/{id}")
    public String showEditLectureForm(@PathVariable Long id, Model model, Principal principal) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture Id:" + id));
        
        // Перевірка, чи це курс цього викладача
        checkCourseOwnership(lecture.getCourse().getId(), principal);
        
        model.addAttribute("lecture", lecture);
        return "add-lecture"; // Використовуємо існуючий шаблон
    }
}