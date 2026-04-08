package com.university.system.repository;

import com.university.system.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudentIdAndTestCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndTestId(Long studentId, Long testId);
    List<Result> findByStudentId(Long studentId);
    @Query("SELECT r FROM Result r JOIN FETCH r.test t JOIN FETCH t.course c WHERE r.student.id = :studentId")
    List<Result> findByStudentIdWithTestAndCourse(@Param("studentId") Long studentId);
}