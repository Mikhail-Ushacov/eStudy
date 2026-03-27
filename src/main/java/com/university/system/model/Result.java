package com.university.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY
    @JoinColumn(name = "student_id")   // Added JoinColumn explicitly
    private User student;

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY
    @JoinColumn(name = "test_id")      // Added JoinColumn explicitly
    private Test test;

    private int score;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}