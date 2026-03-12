package com.university.system.repository;

import com.university.system.model.Course;
import org.springframework.data.jpa.repository.JpaRepository; // Missing import
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long id);
}