package com.saferide.SaferideBackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.model.AttendanceRecord;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

//Acts as a middle layer between Controller and Database
@Service
public class AttendanceService {

    private static final String COLLECTION_NAME = "attendance";

    public String saveRecord(AttendanceRecord record) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        // 1. Duplicate Scan Prevention (same student, same status, same trip, within 1
        // minute)
        if (isDuplicateScan(record)) {
            throw new RuntimeException("Duplicate scan detected for student: " + record.getStudentId());
        }

        String recordId = java.util.Objects.requireNonNull(record.getRecordId(), "Record ID must not be null");

        try {
            // 2. Save Attendance Record
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME).document(recordId)
                    .set(record);
            String updateTime = collectionsApiFuture.get().getUpdateTime().toString();

            // 3. Update Student Status in Student Collection
            DocumentReference studentRef = dbFirestore.collection("students")
                    .document(java.util.Objects.requireNonNull(record.getStudentId()));
            studentRef.update("currentStatus", record.getStatus()).get();

            return updateTime;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving attendance record: " + e.getMessage());
        }
    }

    private boolean isDuplicateScan(AttendanceRecord record) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            // Check for records of same student, same trip, same status in the last 1
            // minute
            long oneMinuteAgo = System.currentTimeMillis() - 60000;
            QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                    .whereEqualTo("studentId", record.getStudentId())
                    .whereEqualTo("status", record.getStatus())
                    .whereEqualTo("tripId", record.getTripId())
                    .whereGreaterThan("scanTime", new java.util.Date(oneMinuteAgo))
                    .get().get();

            return !querySnapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}

// Input: AttendanceRecord object...Output: timestamp (String)
// AttendanceRecord gets saved into Firestore.(collection)
// attendance
// ├── record1
// ├── studentId: S001
// ├── status: PICKED_UP
// ├── scanTime: 2026-03-21 08:30
// ├── record2
// ├── record3
// Firestore automatically converts:AttendanceRecord → JSON-like document