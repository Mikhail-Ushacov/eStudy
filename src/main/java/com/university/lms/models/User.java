package com.university.lms.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data // Lombok генерує геттери, сеттери
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String fullName;
    private String role; // ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
}