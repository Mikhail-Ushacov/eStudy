package com.university.system.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY
    @JoinColumn(name = "course_id")   // Added JoinColumn explicitly
    private Course course;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions;

    private boolean isFinal = false; // Indicates if this is the final test for the course

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean aFinal) { isFinal = aFinal; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}