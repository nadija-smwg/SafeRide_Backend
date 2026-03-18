package com.codex.saferidebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_sessions")
@Data
public class TripSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver; // Links the trip to the specific driver

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String currentGps; // Stores the live coordinates
    private String status; // e.g., "STARTED", "ENROUTE", "ENDED"
}