package com.saferide.SaferideBackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private static final String COLLECTION_NAME = "students";

    // ─── SAVE (Add new student) ─────────────────────────────────────────────
    public String saveStudent(Student student) {
        // Auto-generate a unique student ID if not provided
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            student.setStudentId("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        Firestore db = FirestoreClient.getFirestore();
        String studentId = java.util.Objects.requireNonNull(student.getStudentId(), "Student ID must not be null");
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(studentId)
                .set(student);
        try {
            future.get();
            return student.getStudentId(); // return the new student ID
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error saving student: " + e.getMessage());
            return null;
        }
    }

    // ─── GET by ID ──────────────────────────────────────────────────────────
    public Student getStudentById(String studentId) {
        Firestore db = FirestoreClient.getFirestore();
        String safeStudentId = java.util.Objects.requireNonNull(studentId, "studentId must not be null");
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(safeStudentId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Student.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching student " + studentId + ": " + e.getMessage());
            return null;
        }
    }

    // ─── GET ALL students ───────────────────────────────────────────────────
    public List<Student> getAllStudents() {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<Student> students = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                students.add(doc.toObject(Student.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return students;
    }

    // ─── DELETE student ─────────────────────────────────────────────────────
    public String deleteStudent(String studentId) {
        Firestore db = FirestoreClient.getFirestore();
        String safeStudentId = java.util.Objects.requireNonNull(studentId, "studentId must not be null");
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(safeStudentId).delete();
        try {
            future.get();
            return "Student " + studentId + " deleted successfully.";
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting student " + studentId + ": " + e.getMessage());
            return null;
        }
    }
}
