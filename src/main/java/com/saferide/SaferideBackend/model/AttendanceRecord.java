package com.saferide.SaferideBackend.model;

//Use java.util.Date for Firestore compatibility.Firestore does not natively support java.time.LocalDateTime
//When AttendanceService calls .set(record), Firestore looks for getScanTime().
import java.util.Date;

public class AttendanceRecord {
    private String recordId; //auto-generated
    private String studentId;
    private String status;
    private Date scanTime;

    public AttendanceRecord() {}

    public AttendanceRecord(String recordId, String studentId, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.status = status;
        this.scanTime = new Date (); //more reliable.backend controls time
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getScanTime() { return scanTime; }
    public void setScanTime(Date scanTime) { this.scanTime = scanTime; }
}

//Every time a student scans a QR code,AttendanceRecord is created