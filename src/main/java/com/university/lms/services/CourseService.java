package com.university.lms.services;

import com.university.lms.models.*;
import com.university.lms.repositories.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TestResultRepository testResultRepository;
    private final TestRepository testRepository;

    public Course findById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Курс не знайдено"));
    }

    public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, 
                         TestResultRepository testResultRepository, TestRepository testRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.testResultRepository = testResultRepository;
        this.testRepository = testRepository;
    }

    public void enrollStudent(User student, Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        // Перевірка чи вже записаний
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Студент вже записаний на цей курс!");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus("ACTIVE");
        enrollmentRepository.save(enrollment);
    }

    // Перевірка доступу до фінального тесту (ТЗ: Д.5)
    public boolean canTakeFinalTest(User student, Long courseId) {
        long totalRegularTests = testRepository.countByCourseIdAndIsFinalFalse(courseId);
        long passedTests = testResultRepository.countPassedRegularTests(student.getId(), courseId);
        return totalRegularTests == passedTests;
    }
}