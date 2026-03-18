package com.codex.saferidebackend.service;

import com.codex.saferidebackend.model.AttendanceRecord;
import com.codex.saferidebackend.model.Student;
import com.codex.saferidebackend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service // Tells Spring this is where the business logic lives
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // This method handles what happens when a QR code is scanned
    public String processQrScan(String qrCode) {
        // 1. Use the repo method you created to find the student
        Student student = studentRepository.findByQrcodeData(qrCode);

        if (student == null) {
            return "Error: Student not found!";
        }

        // 2. Log the logic (In a full version, you'd save to AttendanceRepository here)
        System.out.println("Scanning student: " + student.getStudentName());

        // 3. Return a success message to the Driver's App
        return "Success: " + student.getStudentName() + " has been checked in.";
    }
}
