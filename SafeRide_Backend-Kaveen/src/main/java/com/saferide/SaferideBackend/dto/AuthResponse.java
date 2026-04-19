package com.saferide.SaferideBackend.dto;

public class AuthResponse {
    private String token;
    private String uid;
    private String email;
    private String role;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String token, String uid, String email, String role, String message) {
        this.token = token;
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
