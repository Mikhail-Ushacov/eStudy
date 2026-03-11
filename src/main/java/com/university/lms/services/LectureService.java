package com.university.lms.services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class LectureService {
    public List<Object> findByCourseId(Long courseId) {
        return new ArrayList<>(); // Тимчасово повертаємо порожній список
    }
}