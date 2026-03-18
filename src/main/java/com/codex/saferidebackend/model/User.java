package com.codex.saferidebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // This creates the "users" table in MySQL
@Data // Lombok automatically creates Getters and Setters
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // Your Primary Key

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // DRIVER or PARENT

    @Column(length = 500)
    private String authToken; // The token for authorization

    private LocalDateTime tokenExpiry;
}