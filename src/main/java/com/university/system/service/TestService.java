package com.university.system.service;

import com.university.system.model.Question;
import com.university.system.model.Test;
import com.university.system.repository.QuestionRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TestService {
    @Autowired private QuestionRepository questionRepo;
    @Autowired private ResultRepository resultRepo;
    @Autowired private TestRepository testRepo;

    public int calculateScore(Long testId, Map<String, String> answers) {
        List<Question> questions = questionRepo.findByTestId(testId);
        int total = 0;
        for (Question q : questions) {
            String submitted = answers.get("q" + q.getId());
            if (q.getCorrectAnswer() != null && q.getCorrectAnswer().equalsIgnoreCase(submitted)) {
                total += q.getPoints();
            }
        }
        return total;
    }

    public boolean canTakeFinalTest(Long studentId, Long courseId) {
        List<Test> allTests = testRepo.findByCourseId(courseId);
        List<Test> regularTests = allTests.stream().filter(t -> !t.isFinal()).toList();
        
        for (Test t : regularTests) {
            if (!resultRepo.existsByStudentIdAndTestId(studentId, t.getId())) {
                return false;
            }
        }
        return true;
    }
}