package com.university.system.controller;

import com.university.system.model.Course;
import com.university.system.model.Lecture;
import com.university.system.model.LectureSection;
import com.university.system.repository.LectureRepository;
import com.university.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lectures")
public class LectureController {

    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private CourseService courseService;

    @GetMapping("/{id}")
    public String viewLecture(@PathVariable Long id, Model model) {
        Lecture lecture = lectureRepository.findById(id)
                                          .orElseThrow(() -> new IllegalArgumentException("Invalid lecture Id:" + id));
        model.addAttribute("lecture", lecture);
        return "lecture";
    }

    // Teacher can add/manage lectures - methods moved from TeacherController
    @GetMapping("/add/{courseId}")
    public String showAddLectureForm(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        Lecture lecture = new Lecture();
        lecture.setCourse(course); // Pre-set the course
        model.addAttribute("lecture", lecture);
        return "add-lecture";
    }

    @PostMapping("/add/{courseId}")
    public String addLecture(@PathVariable Long courseId, @ModelAttribute Lecture lecture) {
        Course course = courseService.getCourseById(courseId).orElseThrow();
        lecture.setCourse(course);
        
        // Прив'язуємо розділи до лекції перед збереженням
        if (lecture.getSections() != null) {
            for (int i = 0; i < lecture.getSections().size(); i++) {
                LectureSection section = lecture.getSections().get(i);
                section.setLecture(lecture);
                section.setOrderIndex(i);
            }
        }
        
        lectureRepository.save(lecture);
        return "redirect:/teacher/course/" + courseId;
    }

    @PostMapping("/delete/{id}")
    public String deleteLecture(@PathVariable Long id) {
        Lecture lecture = lectureRepository.findById(id)
                                          .orElseThrow(() -> new IllegalArgumentException("Invalid lecture Id:" + id));
        Long courseId = lecture.getCourse().getId();
        lectureRepository.deleteById(id);
        return "redirect:/teacher/course/" + courseId;
    }
}