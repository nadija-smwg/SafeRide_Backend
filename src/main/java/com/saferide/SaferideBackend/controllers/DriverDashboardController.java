package com.saferide.SaferideBackend.controllers;

import com.saferide.SaferideBackend.models.Driver;
import com.saferide.SaferideBackend.models.Student;
import com.saferide.SaferideBackend.services.DriverDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver-dashboard")
@CrossOrigin(origins = "*")
public class DriverDashboardController {

    @Autowired
    private DriverDashboardService driverService;

    @PostMapping("/drivers/{driverId}/students/{studentId}")
    public ResponseEntity<?> linkStudent(@PathVariable String driverId, @PathVariable String studentId) {
        try {
            driverService.linkStudentToDriver(driverId, studentId);
            return ResponseEntity.ok("Student linked successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/drivers/{driverId}/students")
    public ResponseEntity<?> getAssignedStudents(@PathVariable String driverId) {
        try {
            List<Student> students = driverService.getStudentsByDriver(driverId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/scan/{studentId}")
    public ResponseEntity<?> handleScan(@PathVariable String studentId, @RequestParam boolean isPickup) {
        try {
            String newStatus = driverService.scanStudent(studentId, isPickup);
            return ResponseEntity.ok(newStatus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/students/{studentId}/status")
    public ResponseEntity<?> updateStudentStatus(@PathVariable String studentId, @RequestParam String status) {
        try {
            driverService.updateStudentStatus(studentId, status);
            return ResponseEntity.ok("Status updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/drivers/{driverId}/location")
    public ResponseEntity<?> updateDriverLocation(@PathVariable String driverId, @RequestParam double latitude, @RequestParam double longitude) {
        try {
            driverService.updateDriverLocation(driverId, latitude, longitude);
            return ResponseEntity.ok("Location updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/drivers/{driverId}/location")
    public ResponseEntity<?> getDriverLocation(@PathVariable String driverId) {
        try {
            Driver driver = driverService.getDriverLocation(driverId);
            return ResponseEntity.ok(driver);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
