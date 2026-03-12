package com.university.system.repository;

import com.university.system.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudentIdAndTestCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndTestId(Long studentId, Long testId);
}