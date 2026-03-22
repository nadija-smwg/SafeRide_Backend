package com.saferide.SaferideBackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
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
        Firestore dbFirestore = FirestoreClient.getFirestore(); //getting firestore instance(Connects to Firebase Firestore,Uses serviceAccountKey.json)

        // We use the recordId generated within AttendanceRecord as the document ID(without DOC ID cannot store properly)
        String recordId = java.util.Objects.requireNonNull(record.getRecordId(), "Record ID must not be null");
        //saving data to Firestore
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME).document(recordId).set(record);
        //ApiFuture<WriteResult>--- Save DB operation runs in background, Returns a "future result"
        try {
            return collectionsApiFuture.get().getUpdateTime().toString(); //Returns WriteResult and get Time when Firestore saved the record
        }
        //Network issues,server shutting down or Firebase failure
        catch (InterruptedException | ExecutionException e) {
            System.err.println("Error saving attendance record " + record.getRecordId() + ": " + e.getMessage());
            return null;
        }
    }
}

//Input: AttendanceRecord object...Output: timestamp (String)
//AttendanceRecord gets saved into Firestore.(collection)
//attendance
// ├── record1
//      ├── studentId: S001
//      ├── status: PICKED_UP
//      ├── scanTime: 2026-03-21 08:30
// ├── record2
// ├── record3
//Firestore automatically converts:AttendanceRecord → JSON-like document