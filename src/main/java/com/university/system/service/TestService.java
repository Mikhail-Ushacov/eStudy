package com.university.system.service;

import com.university.system.model.AnswerOption;
import com.university.system.model.Question;
import com.university.system.model.Test;
import com.university.system.repository.QuestionRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

// import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestService {
    @Autowired private QuestionRepository questionRepo;
    @Autowired private ResultRepository resultRepo;
    @Autowired private TestRepository testRepo;

    public int calculateScore(Long testId, MultiValueMap<String, String> selectedAnswers) {
        List<Question> questions = questionRepo.findByTestId(testId);
        int totalScore = 0;

        for (Question q : questions) {
            // Отримуємо ID всіх правильних варіантів для цього питання
            Set<Long> correctOptionIds = q.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());

            // Отримуємо ID варіантів, які вибрав студент (з параметрів запиту)
            List<String> userSelectedStrings = selectedAnswers.get("q" + q.getId());
            
            if (userSelectedStrings != null && !userSelectedStrings.isEmpty()) {
                Set<Long> userSelectedIds = userSelectedStrings.stream()
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());

                // Якщо набори ID повністю збігаються - нараховуємо бали
                if (correctOptionIds.equals(userSelectedIds)) {
                    totalScore += q.getPoints();
                }
            }
        }
        return totalScore;
    }

    public boolean canTakeFinalTest(Long studentId, Long courseId) {
        List<Test> allTests = testRepo.findByCourseId(courseId);
        List<Test> regularTests = allTests.stream().filter(t -> !t.isFinalTest()).toList();
        
        for (Test t : regularTests) {
            if (!resultRepo.existsByStudentIdAndTestId(studentId, t.getId())) {
                return false;
            }
        }
        return true;
    }
}