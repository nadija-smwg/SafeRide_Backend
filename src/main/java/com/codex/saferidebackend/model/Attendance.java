package com.codex.saferidebackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;
    private String status;
    private LocalDateTime timestamp;

    public Attendance() {}

    // --- Add these Setters and Getters ---
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}