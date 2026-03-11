package com.university.lms.controllers;

import com.university.lms.models.Course;
import com.university.lms.models.User;
import com.university.lms.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final LectureService lectureService;
    private final TestService testService;

    public CourseController(CourseService courseService, UserService userService, 
                            LectureService lectureService, TestService testService) {
        this.courseService = courseService;
        this.userService = userService;
        this.lectureService = lectureService;
        this.testService = testService;
    }

    @GetMapping("/course/{id}")
    public String viewCourse(@PathVariable Long id, Model model, Principal principal) {
        User student = userService.findByUsername(principal.getName());
        Course course = courseService.findById(id);
        
        boolean canTakeFinal = courseService.canTakeFinalTest(student, id);
        
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureService.findByCourseId(id));
        model.addAttribute("tests", testService.findRegularTests(id));
        model.addAttribute("finalTest", testService.findFinalTest(id));
        model.addAttribute("canTakeFinal", canTakeFinal);
        
        return "course_view";
    }
}