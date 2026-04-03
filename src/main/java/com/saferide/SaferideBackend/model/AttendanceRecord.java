package com.saferide.SaferideBackend.model;

//Use java.util.Date for Firestore compatibility.Firestore does not natively support java.time.LocalDateTime
//When AttendanceService calls .set(record), Firestore looks for getScanTime().
import java.util.Date;

public class AttendanceRecord {
    private String recordId; //auto-generated
    private String studentId;
    private String tripId; // associated trip
    private String status;
    private Date scanTime;
    private Double latitude;
    private Double longitude;
    private boolean isManual; // true if driver marked attendance without QR

    public AttendanceRecord() {}

    public AttendanceRecord(String recordId, String studentId, String status, String tripId, Double latitude, Double longitude, boolean isManual) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.status = status;
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isManual = isManual;
        this.scanTime = new Date(); //more reliable.backend controls time
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getScanTime() { return scanTime; }
    public void setScanTime(Date scanTime) { this.scanTime = scanTime; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean isManual() { return isManual; }
    public void setManual(boolean manual) { isManual = manual; }
}

//Every time a student scans a QR code,AttendanceRecord is created