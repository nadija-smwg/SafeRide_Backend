package com.saferide.saferide_backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.saferide_backend.model.Student;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private static final String COLLECTION_NAME = "students";

    public Student getStudentById(String studentId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(studentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Student.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching student ID " + studentId + ": " + e.getMessage());
            return null;
        }
    }
}
