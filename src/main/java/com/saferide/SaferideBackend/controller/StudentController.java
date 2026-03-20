package com.saferide.SaferideBackend.controller;

import com.saferide.SaferideBackend.model.Student;
import com.saferide.SaferideBackend.service.NotificationService;
import com.saferide.SaferideBackend.service.QRCodeService;
import com.saferide.SaferideBackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*") // Allow frontend to call this API
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private NotificationService notificationService;

    // ─── POST /api/students/add ─────────────────────────────────────────────
    // Registers a new student, saves to Firestore,
    // generates QR code, and emails it to the parent
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addStudent(@RequestBody Student student) {
        // 1. Save student to Firestore — returns the auto-generated studentId
        String studentId = studentService.saveStudent(student);

        if (studentId == null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to save student to database."));
        }

        // 2. Reload the saved student to get the assigned studentId
        student.setStudentId(studentId);

        // 3. Generate the QR code for this student
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(studentId);

        // 4. Email the QR code to the parent
        notificationService.sendQRCodeToParent(student, qrCodeBase64);

        // 5. Return success response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Student registered successfully!");
        response.put("studentId", studentId);
        response.put("qrCode", qrCodeBase64); // Frontend can display this immediately
        return ResponseEntity.ok(response);
    }

    // ─── GET /api/students/{studentId} ─────────────────────────────────────
    // Get a single student's details from Firestore
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId) {
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    // ─── GET /api/students/all ──────────────────────────────────────────────
    // Get all students from Firestore (for admin panel / dashboard)
    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // ─── DELETE /api/students/{studentId} ──────────────────────────────────
    // Delete a student from Firestore
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable String studentId) {
        String result = studentService.deleteStudent(studentId);
        if (result == null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete student."));
        }
        return ResponseEntity.ok(Map.of("message", result));
    }

    // ─── GET /api/students/qr/{studentId} ──────────────────────────────────
    // Re-generate and return the QR code for an existing student
    @GetMapping("/qr/{studentId}")
    public ResponseEntity<Map<String, String>> getStudentQR(@PathVariable String studentId) {
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(studentId);
        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "studentName", student.getStudentName(),
                "qrCode", qrCodeBase64
        ));
    }
}
