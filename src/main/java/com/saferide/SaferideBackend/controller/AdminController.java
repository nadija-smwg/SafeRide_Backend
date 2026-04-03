package com.saferide.SaferideBackend.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Firestore db = FirestoreClient.getFirestore();
        try {
            // fetches all student documents and counts them
            long totalStudents = studentService.getAllStudents().size();

            // For simplicity, we count records from today in the attendance collection
            // In a production app, we would use a more efficient query or indexed counter
            QuerySnapshot todayAttendance = db.collection("attendance")
                    .whereGreaterThan("scanTime", java.util.Objects.requireNonNull(getStartOfToday()))
                    .get().get();

            long attendanceCount = todayAttendance.size();

            QuerySnapshot activeTrips = db.collection("trips")
                    .whereEqualTo("status", "ONGOING")
                    .get().get();
            // Active trips count?
            long activeTripCount = activeTrips.size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", totalStudents);
            stats.put("todayAttendanceCount", attendanceCount);
            stats.put("activeTrips", activeTripCount);
            // Return JSON stats
            return ResponseEntity.ok(stats);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    private java.util.Date getStartOfToday() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
        // Resets current time to 00:00:00,useful for querying Firestore for “today”
        // records.
    }

    @GetMapping("/history/student/{studentId}")
    public ResponseEntity<?> getStudentHistory(@PathVariable String studentId) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            QuerySnapshot history = db.collection("attendance")
                    .whereEqualTo("studentId", studentId)
                    .orderBy("scanTime", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get().get();

            // Converts Firestore documents to generic maps (Map<String,Object>)
            return ResponseEntity.ok(history.toObjects(Map.class));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().body("Error fetching history: " + e.getMessage());
        }
    }

    @GetMapping("/history/trips")
    public ResponseEntity<?> getAllTripHistory() {
        Firestore db = FirestoreClient.getFirestore();
        try {
            // Fetch all trips, ordered by startTime descending
            QuerySnapshot trips = db.collection("trips")
                    .orderBy("startTime", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get().get();
            return ResponseEntity.ok(trips.toObjects(Map.class));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().body("Error fetching trip history: " + e.getMessage());
        }
    }

    @GetMapping("/history/trips/driver/{driverId}")
    public ResponseEntity<?> getDriverTripHistory(@PathVariable String driverId) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            // Query trips by driverId
            QuerySnapshot trips = db.collection("trips")
                    .whereEqualTo("driverId", driverId)
                    .orderBy("startTime", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get().get();
            return ResponseEntity.ok(trips.toObjects(Map.class));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().body("Error fetching driver trip history: " + e.getMessage());
        }
    }
}
