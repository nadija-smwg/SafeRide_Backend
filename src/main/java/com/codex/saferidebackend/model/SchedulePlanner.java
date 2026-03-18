package com.codex.saferidebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "schedule_planner")
@Data
public class SchedulePlanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // Each student has one main schedule

    private String weeklyPlan; // e.g., "Mon-Fri: Morning & Evening"

    private boolean isAbsentToday; // To handle the "Report Daily Absence" feature

    private String dailyNote; // For special instructions from parents
}
