package com.university.lms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Для спрощення використовуємо Object, якщо клас Test ще не створено
@Repository
public interface TestRepository extends JpaRepository<com.university.lms.models.User, Long> { 
    long countByCourseIdAndIsFinalFalse(Long courseId);
}