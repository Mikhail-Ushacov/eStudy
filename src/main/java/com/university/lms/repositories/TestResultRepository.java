package com.university.lms.repositories;

import com.university.lms.models.User; // Або створіть окрему модель TestResult
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TestResultRepository extends JpaRepository<User, Long> {
    @Query("SELECT COUNT(tr) FROM User tr WHERE tr.id = ?1") // Заглушка для логіки підрахунку
    long countPassedRegularTests(Long studentId, Long courseId);
}