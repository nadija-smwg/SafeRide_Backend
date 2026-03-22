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

@RestController //handles HTTP requests(API endpoints),returns data/respones as JSON
@RequestMapping("/api/scan") //all endpoints start with this like http://localhost:8080/api/scan.
@CrossOrigin(origins = "*") // Allow frontend to call these endpoints/backend
public class ScanController {

    @Autowired //Spring automatically provides objects
    private NotificationService notificationService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/pickup/{studentId}") //endpoint POST /api/scan/pickup/STU-1234
    public ResponseEntity<?> scanPickup(@PathVariable String studentId) { //get studentId from URL
        // 1. Fetch student from Firebase(calls Firestore and gets Student object)
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found in Firebase with ID: " + studentId); //404 Not Found
        }

        // 2. Create and Save Attendance Record in DB
        AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, "PICKUP"); //recordID random
        attendanceService.saveRecord(record);

        // 3. Notify parent
        notificationService.sendPickupNotification(student);

        return ResponseEntity.ok(record); //frontend receives JSON record
    }

    @PostMapping("/dropoff/{studentId}") //endpoint POST /api/scan/dropoff/STU-1234
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

    //generate QR API
    @GetMapping("/generate/{studentId}") //endpoint GET /api/scan/generate/STU-1234
    public ResponseEntity<String> generateQR(@PathVariable String studentId) {
        // Returns the Base64 image which frontend can use like: <img src="data:image/png;base64,....." />
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(studentId); //generates QR image into Base64 string
        return ResponseEntity.ok(qrCodeBase64); //frontend receives iVBORw0KGgoAAAANSUhEUgAA... and displays QR image
    }
}
