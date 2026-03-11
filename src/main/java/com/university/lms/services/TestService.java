package com.university.lms.services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestService {
    public List<Object> findRegularTests(Long courseId) {
        return new ArrayList<>();
    }

    public Object findFinalTest(Long courseId) {
        return null;
    }
}