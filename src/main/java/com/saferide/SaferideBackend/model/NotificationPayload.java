package com.saferide.SaferideBackend.model;

import java.time.LocalDateTime;

public class NotificationPayload {
    private String alertType;
    private String parentEmail;
    private String subject;
    private String studentName;
    private String studentId;//link with records
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

    //Firestore / Spring / JSON frameworks rely on these methods to map object fields.
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

//oes not interact with Firestore or QR scans directly; it’s only used for sending notifications.it’s just a message object.