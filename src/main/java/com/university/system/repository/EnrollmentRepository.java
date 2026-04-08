package com.university.system.repository;

import com.university.system.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByStudentIdAndConfirmed(Long studentId, boolean confirmed);
    List<Enrollment> findByCourseIdAndConfirmed(Long courseId, boolean confirmed);
    List<Enrollment> findByConfirmed(boolean confirmed);
    Enrollment findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
