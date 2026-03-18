package com.codex.saferidebackend.controller;

import com.codex.saferidebackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students") // The base URL for all student actions
public class StudentController {

    @Autowired
    private StudentService studentService;

    // This creates the URL: http://localhost:8080/api/students/scan?qrCode=XYZ
    @PostMapping("/scan")
    public String scanStudent(@RequestParam String qrCode) {
        return studentService.processQrScan(qrCode);
    }
}
