package com.saferide.SaferideBackend.controllers;

import com.saferide.SaferideBackend.dto.StudentRequest;
import com.saferide.SaferideBackend.models.ScheduleItem;
import com.saferide.SaferideBackend.models.Student;
import com.saferide.SaferideBackend.services.ParentStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("parentStudentController")
@RequestMapping("/api/parent/students")
@CrossOrigin(origins = "*")
public class ParentStudentController {

    @Autowired
    private ParentStudentService parentStudentService;

    @PostMapping("/parent/{parentId}")
    public ResponseEntity<?> addStudent(@PathVariable String parentId, @RequestBody StudentRequest request) {
        try {
            Student student = parentStudentService.addStudent(parentId, request);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<?> getStudentsByParent(@PathVariable String parentId) {
        try {
            List<Student> students = parentStudentService.getStudentsByParent(parentId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable String studentId) {
        try {
            Student student = parentStudentService.getStudentById(studentId);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{studentId}/bio")
    public ResponseEntity<?> updateStudentBio(@PathVariable String studentId, @RequestBody StudentRequest request) {
        try {
            Student student = parentStudentService.updateStudentBio(studentId, request);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{studentId}/schedule")
    public ResponseEntity<?> updateStudentSchedule(@PathVariable String studentId, @RequestBody List<ScheduleItem> schedule) {
        try {
            Student student = parentStudentService.updateStudentSchedule(studentId, schedule);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverDetails(@PathVariable String driverId) {
        try {
            java.util.Map<String, Object> driver = parentStudentService.getDriverDetails(driverId);
            return ResponseEntity.ok(driver);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
