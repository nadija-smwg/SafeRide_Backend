package com.codex.saferidebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(nullable = false)
    private String studentName;

    @Column(unique = true, nullable = false)
    private String qrcodeData; // Must be unique for security

    private String homeAddress;
    private String emergencyContact;

    // This is the "glue" - linking student to a parent user
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;
}
