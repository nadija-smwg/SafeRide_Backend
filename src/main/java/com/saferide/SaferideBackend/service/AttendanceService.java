package com.saferide.saferide_backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.saferide_backend.model.AttendanceRecord;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class AttendanceService {

    private static final String COLLECTION_NAME = "attendance";

    public String saveRecord(AttendanceRecord record) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        // We use the recordId generated within AttendanceRecord as the document ID
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME)
                .document(record.getRecordId())
                .set(record);

        try {
            return collectionsApiFuture.get().getUpdateTime().toString();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error saving attendance record " + record.getRecordId() + ": " + e.getMessage());
            return null;
        }
    }
}
