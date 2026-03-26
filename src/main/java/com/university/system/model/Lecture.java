package com.university.system.model;

import jakarta.persistence.*;

@Entity
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String filePath; // Path to the lecture content (e.g., HTML file, PDF link)

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY
    @JoinColumn(name = "course_id")   // Added JoinColumn explicitly
    private Course course;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}