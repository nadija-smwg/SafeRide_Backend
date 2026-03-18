package com.codex.saferidebackend.service;

import com.codex.saferidebackend.model.Attendance;
import com.codex.saferidebackend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public String processQrScan(String qrCode, String action) {
        // 1. Create the Attendance Record
        Attendance record = new Attendance();
        record.setStudentId(qrCode);

        // 2. Use the 'action' from the URL (ENTRY or EXIT)
        record.setStatus(action.toUpperCase());
        record.setTimestamp(LocalDateTime.now());

        // 3. Save to MySQL
        attendanceRepository.save(record);

        return "Successfully recorded " + action + " for: " + qrCode;
    }
}