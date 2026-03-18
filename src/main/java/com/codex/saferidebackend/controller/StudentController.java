package com.codex.saferidebackend.controller;

import com.codex.saferidebackend.repository.AttendanceRepository;
import com.codex.saferidebackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students") // The base URL for all student actions
public class StudentController {
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private StudentService studentService;

    // This creates the URL: http://localhost:8080/api/students/scan?qrCode=XYZ
    @GetMapping("/scan")
    public String scanStudent(@RequestParam String qrCode, @RequestParam String action) {
        // This now sends both the ID and the Action (Entry/Exit) to the service
        return studentService.processQrScan(qrCode, action);
    }
}