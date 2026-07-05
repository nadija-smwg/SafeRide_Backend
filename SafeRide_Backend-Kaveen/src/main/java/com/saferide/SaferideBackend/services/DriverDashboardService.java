package com.saferide.SaferideBackend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.models.Driver;
import com.saferide.SaferideBackend.models.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class DriverDashboardService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.saferide.SaferideBackend.service.NotificationService notificationService;
    public void linkStudentToDriver(String driverId, String studentId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("students").document(studentId).update("assignedDriverId", driverId).get();
    }

    public List<Student> getStudentsByDriver(String driverId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("students").whereEqualTo("assignedDriverId", driverId).get();
        List<Student> students = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            students.add(document.toObject(Student.class));
        }
        return students;
    }

    public void updateStudentStatus(String studentId, String status) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("students").document(studentId).update("status", status).get();
    }

    public String scanStudent(String studentId, boolean isPickup) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("students").document(studentId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Student not found");
        }
        Student student = doc.toObject(Student.class);

        // ── Validate session mode ──
        String driverId = student.getAssignedDriverId();
        if (driverId != null && !driverId.isEmpty()) {
            String sessionMode = getSessionMode(driverId);
            if (sessionMode == null || "NONE".equals(sessionMode)) {
                throw new RuntimeException("No active session. Please enable a session mode before scanning.");
            }
            boolean isMorningSession = "MORNING".equals(sessionMode);
            if (isPickup && !isMorningSession) {
                throw new RuntimeException("Morning session is not active. Cannot perform pickup scan.");
            }
            if (!isPickup && isMorningSession) {
                throw new RuntimeException("Afternoon session is not active. Cannot perform dropoff scan.");
            }
        }

        String currentStatus = student.getStatus();
        String newStatus = currentStatus;

        if (isPickup) {
            if ("AT_HOME".equals(currentStatus)) newStatus = "IN_TRANSIT";
            else if ("IN_TRANSIT".equals(currentStatus)) newStatus = "IN_SCHOOL";
        } else {
            if ("IN_SCHOOL".equals(currentStatus)) newStatus = "IN_TRANSIT";
            else if ("IN_TRANSIT".equals(currentStatus)) newStatus = "AT_HOME";
        }
        
        db.collection("students").document(studentId).update("status", newStatus).get();
        return newStatus;
    }

    // ── Session Mode Management ──

    public void setSessionMode(String driverId, String mode) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("drivers").document(driverId).set(
                Map.of("activeSessionMode", mode), SetOptions.merge()
        ).get();
    }

    public String getSessionMode(String driverId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("drivers").document(driverId).get().get();
        if (doc.exists() && doc.contains("activeSessionMode")) {
            return doc.getString("activeSessionMode");
        }
        return null;
    }

    public void updateDriverLocation(String driverId, double latitude, double longitude) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("drivers").document(driverId).set(Map.of("latitude", latitude, "longitude", longitude), SetOptions.merge()).get();
    }

    public Driver getDriverLocation(String driverId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("drivers").document(driverId).get().get();
        if (doc.exists()) {
            return doc.toObject(Driver.class);
        }
        return new Driver();
    }
    public Driver getDriverProfile(String driverId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("drivers").document(driverId).get().get();
        if (doc.exists()) {
            Driver driver = doc.toObject(Driver.class);
            driver.setId(driverId);
            return driver;
        }
        return new Driver();
    }

    public void updateDriverProfile(String driverId, Driver updateData) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> updates = new java.util.HashMap<>();
        if (updateData.getFullName() != null) updates.put("fullName", updateData.getFullName());
        if (updateData.getPhoneNumber() != null) updates.put("phoneNumber", updateData.getPhoneNumber());
        if (updateData.getLicenseNumber() != null) updates.put("licenseNumber", updateData.getLicenseNumber());
        if (updateData.getVehicleNumber() != null) updates.put("vehicleNumber", updateData.getVehicleNumber());
        if (updateData.getProfileImageBase64() != null) updates.put("profileImageBase64", updateData.getProfileImageBase64());
        
        db.collection("drivers").document(driverId).set(updates, SetOptions.merge()).get();
    }

    public void broadcastMessageToStudents(String driverId, String title, String message) throws ExecutionException, InterruptedException {
        List<Student> assignedStudents = getStudentsByDriver(driverId);
        Driver driverProfile = getDriverProfile(driverId);
        String finalTitle = (title != null && !title.isEmpty()) ? title : "Message from Driver " + driverProfile.getFullName();

        for (Student student : assignedStudents) {
            if (student.getParentFcmToken() != null && !student.getParentFcmToken().isEmpty()) {
                notificationService.sendPushNotification(student.getParentFcmToken(), finalTitle, message);
            }
        }
    }
}
