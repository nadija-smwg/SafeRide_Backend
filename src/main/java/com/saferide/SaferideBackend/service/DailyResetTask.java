package com.saferide.SaferideBackend.service;

import com.saferide.SaferideBackend.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyResetTask {

    @Autowired
    private StudentService studentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private com.google.cloud.firestore.Firestore db;

    // Run every day at midnight (00:00:00)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetStudentStatuses() {
        System.out.println("Starting daily status reset and summaries...");
        
        List<Student> students = studentService.getAllStudents();
        
        // 1. Send Daily Summaries (simplified grouping by parent email)
        sendDailySummaries(students);

        // 2. Reset Statuses
        for (Student student : students) {
            try {
                studentService.updateStudentStatus(student.getStudentId(), "AT_HOME");
            } catch (Exception e) {
                System.err.println("Failed to reset status for student: " + student.getStudentId());
            }
        }

        // 3. Cleanup Stale Trips (ONGOING trips left overnight)
        cleanupStaleTrips();

        System.out.println("Daily status reset completed.");
    }

    private void cleanupStaleTrips() {
        try {
            com.google.cloud.firestore.QuerySnapshot ongoingTrips = db.collection("trips")
                    .whereEqualTo("status", "ONGOING")
                    .get().get();
            
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : ongoingTrips) {
                String tripId = doc.getId();
                tripService.endTrip(tripId);
                System.out.println("Auto-completed stale trip: " + tripId);
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up stale trips: " + e.getMessage());
        }
    }

    private void sendDailySummaries(List<Student> students) {
        // Group students by parent email and send summary of their day
        // In a real app, we'd query attendance records for the day for each student
        for (Student student : students) {
             notificationService.sendDailySummary(student);
        }
    }
}
