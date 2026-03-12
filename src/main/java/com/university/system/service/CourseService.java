package com.university.system.service;

import com.university.system.model.Course;
import com.university.system.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

@Autowired
private CourseRepository courseRepository;

public List<Course> getAllCourses() {
return courseRepository.findAll();
}

public Course saveCourse(Course course) {
return courseRepository.save(course);
}

public void deleteCourse(Long id) {
courseRepository.deleteById(id);
}

}