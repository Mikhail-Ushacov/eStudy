package com.university.system.repository;

import com.university.system.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long id);
    
    // Spring тепер зможе знайти це поле в класі Course
    List<Course> findByEnrollments_Student_Id(Long studentId); 
}