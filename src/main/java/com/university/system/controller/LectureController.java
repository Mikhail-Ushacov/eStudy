package com.university.system.controller;

import com.university.system.model.Lecture;
import com.university.system.repository.LectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lectures")
public class LectureController {

    @Autowired
    private LectureRepository lectureRepository;

    @GetMapping("/{id}")
    public String viewLecture(@PathVariable Long id, Model model) {
        Lecture lecture = lectureRepository.findById(id)
                                          .orElseThrow(() -> new IllegalArgumentException("Invalid lecture Id:" + id));
        model.addAttribute("lecture", lecture);
        return "lecture";
    }
   
}