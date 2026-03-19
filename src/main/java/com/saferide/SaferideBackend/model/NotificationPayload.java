package com.saferide.saferide_backend.model;

import java.time.LocalDateTime;

public class NotificationPayload {
    private String alertType;
    private String parentEmail;
    private String subject;
    private String studentName;
    private String studentId;
    private LocalDateTime timestamp;

    public NotificationPayload() {}

    public NotificationPayload(String alertType, String parentEmail, String subject,
                               String studentName, String studentId) {
        this.alertType = alertType;
        this.parentEmail = parentEmail;
        this.subject = subject;
        this.studentName = studentName;
        this.studentId = studentId;
        this.timestamp = LocalDateTime.now();
    }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
