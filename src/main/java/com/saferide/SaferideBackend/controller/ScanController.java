package com.saferide.SaferideBackend.controller;

import com.saferide.SaferideBackend.model.AttendanceRecord;
import com.saferide.SaferideBackend.model.Student;
import com.saferide.SaferideBackend.service.AttendanceService;
import com.saferide.SaferideBackend.service.NotificationService;
import com.saferide.SaferideBackend.service.QRCodeService;
import com.saferide.SaferideBackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/scan")
@CrossOrigin(origins = "*") // Allow frontend to call these endpoints
public class ScanController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/pickup/{studentId}")
    public ResponseEntity<?> scanPickup(@PathVariable String studentId) {
        // 1. Fetch student from Firebase
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found in Firebase with ID: " + studentId);
        }

        // 2. Create and Save Attendance Record
        AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, "PICKUP");
        attendanceService.saveRecord(record);

        // 3. Notify parent
        notificationService.sendPickupNotification(student);

        return ResponseEntity.ok(record);
    }

    @PostMapping("/dropoff/{studentId}")
    public ResponseEntity<?> scanDropoff(@PathVariable String studentId) {
        // 1. Fetch student from Firebase
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found in Firebase with ID: " + studentId);
        }

        // 2. Create and Save Attendance Record
        AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, "DROPOFF");
        attendanceService.saveRecord(record);

        // 3. Notify parent
        notificationService.sendDropoffNotification(student);

        return ResponseEntity.ok(record);
    }

    @GetMapping("/generate/{studentId}")
    public ResponseEntity<String> generateQR(@PathVariable String studentId) {
        // Returns the Base64 image which frontend can use like: <img src="data:image/png;base64,....." />
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(studentId);
        return ResponseEntity.ok(qrCodeBase64);
    }
}
