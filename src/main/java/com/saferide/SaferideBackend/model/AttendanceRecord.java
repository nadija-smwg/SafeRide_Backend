package com.saferide.saferide_backend.model;

import java.time.LocalDateTime;

public class AttendanceRecord {
    private String recordId;
    private String studentId;
    private String status;
    private LocalDateTime scanTime;

    public AttendanceRecord() {}

    public AttendanceRecord(String recordId, String studentId, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.status = status;
        this.scanTime = LocalDateTime.now();
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScanTime() { return scanTime; }
    public void setScanTime(LocalDateTime scanTime) { this.scanTime = scanTime; }
}
