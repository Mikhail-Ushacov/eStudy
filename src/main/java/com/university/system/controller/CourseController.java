package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Lecture;
import com.university.system.model.Test;
import com.university.system.repository.LectureRepository;
import com.university.system.repository.TestRepository;
import com.university.system.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private TestRepository testRepository;

    @GetMapping
    public String getCourses(Model model, Principal principal) {
        // This will show all courses, students will see a filtered list on their dashboard
        // For guests or general viewing, all courses are shown.
        model.addAttribute("courses", courseService.getAllCourses());
        return "home";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.getCourseById(id)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        List<Lecture> lectures = lectureRepository.findByCourseId(id);
        List<Test> tests = testRepository.findByCourseId(id);

        model.addAttribute("course", course);
        model.addAttribute("lectures", lectures);
        model.addAttribute("tests", tests);
        
        // Add logic to check if user is enrolled
        // Add logic to check test completion for final test unlock (done in TestController already)

        return "course";
    }
}