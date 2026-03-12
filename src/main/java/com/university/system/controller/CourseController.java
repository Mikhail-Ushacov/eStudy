package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.service.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courses")
public class CourseController {

@Autowired
private CourseService courseService;

@GetMapping
public String getCourses(Model model){

model.addAttribute("courses", courseService.getAllCourses());

return "home";
}

@PostMapping("/add")
public String addCourse(Course course){

courseService.saveCourse(course);

return "redirect:/courses";
}

}