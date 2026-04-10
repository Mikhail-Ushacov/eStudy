package com.university.system.controller;

import com.university.system.model.Result;
import com.university.system.model.Test;
import com.university.system.model.User;
import com.university.system.repository.QuestionRepository;
import com.university.system.repository.ResultRepository;
import com.university.system.repository.TestRepository;
import com.university.system.repository.UserRepository;
import com.university.system.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/tests")
public class TestController {
    @Autowired private TestRepository testRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private TestService testService;
    @Autowired private ResultRepository resultRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping("/{id}")
    public String viewTest(@PathVariable Long id, Model model, Principal principal) {
        Test test = testRepo.findById(id).orElseThrow();
        User user = userRepo.findByUsername(principal.getName());
        
        if (resultRepo.existsByStudentIdAndTestId(user.getId(), id)) {
            return "redirect:/student/course/" + test.getCourse().getId() + "?error=already_taken";
        }
        
        if (test.isFinalTest() && !testService.canTakeFinalTest(user.getId(), test.getCourse().getId())) {
            return "redirect:/student/course/" + test.getCourse().getId() + "?error=final_locked";
        }
        
        model.addAttribute("test", test);
        model.addAttribute("questions", questionRepo.findByTestId(id));
        return "test";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @RequestParam MultiValueMap<String, String> params, Principal principal) {
        User user = userRepo.findByUsername(principal.getName());
        Test test = testRepo.findById(id).orElseThrow();
        
        if (resultRepo.existsByStudentIdAndTestId(user.getId(), id)) {
             return "redirect:/student/course/" + test.getCourse().getId();
        }

        // Передаємо MultiValueMap у сервіс для підрахунку
        int score = testService.calculateScore(id, params);
        
        Result result = new Result();
        result.setStudent(user);
        result.setTest(test);
        result.setScore(score);
        resultRepo.save(result);

        return "redirect:/student/course/" + test.getCourse().getId();
    }
}