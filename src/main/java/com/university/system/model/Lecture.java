package com.university.system.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime; // Змінено
import org.springframework.format.annotation.DateTimeFormat; // Додано
import jakarta.persistence.*;

@Entity
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime; // Змінено

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime; // Змінено
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LectureSection> sections = new ArrayList<>();

    // Оновлені геттери та сеттери
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // Решта стандартних методів...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public List<LectureSection> getSections() { return sections; }
    public void setSections(List<LectureSection> sections) { this.sections = sections; }
    public void addSection(LectureSection section) {
        sections.add(section);
        section.setLecture(this);
    }
}