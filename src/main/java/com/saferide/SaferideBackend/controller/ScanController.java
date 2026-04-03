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

    @PostMapping("/pickup/{studentId}")
    //PathVariable- Extract student ID from URL and Optional query params for trip and location
    public ResponseEntity<?> scanPickup(@PathVariable String studentId, @RequestParam String tripId, @RequestParam(required = false) Double lat, @RequestParam(required = false) Double lon) {
        try {
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found: " + studentId);
            }
            //Create AttendanceRecord(unique record ID)
            AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, "PICKED_UP", tripId, lat, lon, false);
            attendanceService.saveRecord(record);
            notificationService.sendPickupNotification(student, lat, lon);
            //Return response
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during pickup: " + e.getMessage());
        }
    }

    @PostMapping("/dropoff/{studentId}")
    public ResponseEntity<?> scanDropoff(@PathVariable String studentId, @RequestParam String tripId, @RequestParam(required = false) Double lat, @RequestParam(required = false) Double lon) {
        try {
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found: " + studentId);
            }

            AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, "DROPPED_OFF", tripId, lat, lon, false);
            attendanceService.saveRecord(record);
            notificationService.sendDropoffNotification(student, lat, lon);

            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during dropoff: " + e.getMessage());
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> scanManual(@RequestParam String studentId, @RequestParam String status, @RequestParam String tripId, @RequestParam(required = false) Double lat, @RequestParam(required = false) Double lon) {
        try {
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found: " + studentId);
            }

            AttendanceRecord record = new AttendanceRecord(UUID.randomUUID().toString(), studentId, status, tripId, lat, lon, true);
            attendanceService.saveRecord(record);
            
            if ("PICKED_UP".equals(status)) {
                notificationService.sendPickupNotification(student, lat, lon);
            } else {
                notificationService.sendDropoffNotification(student, lat, lon);
            }

            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during manual attendance: " + e.getMessage());
        }
    }

    //generate QR API
    @GetMapping("/generate/{studentId}") //endpoint GET /api/scan/generate/STU-1234
    public ResponseEntity<String> generateQR(@PathVariable String studentId) {
        // Returns the Base64 image which frontend can use like: <img src="data:image/png;base64,....." />
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(studentId); //generates QR image into Base64 string
        return ResponseEntity.ok(qrCodeBase64); //frontend receives iVBORw0KGgoAAAANSUhEUgAA... and displays QR image
    }
}
