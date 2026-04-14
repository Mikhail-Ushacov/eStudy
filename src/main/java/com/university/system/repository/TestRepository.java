package com.university.system.repository;

import com.university.system.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByCourseId(Long courseId);
    List<Test> findByCourseIdAndFinalTest(Long courseId, boolean isFinal);
}