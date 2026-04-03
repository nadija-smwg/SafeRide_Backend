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
public class StudentService { //for all student database operations

    private static final String COLLECTION_NAME = "students";

    // ─── SAVE (Add new student to DB) ─────────────────────────────────────────────
    public String saveStudent(Student student) {
        // Auto-generate a unique student ID if not provided
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            student.setStudentId("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()); //UUID - a1b2c3d4e5f6,Final - STU-A1B2C3D4
        }

        Firestore db = FirestoreClient.getFirestore(); //connect to Firebase
        String studentId = java.util.Objects.requireNonNull(student.getStudentId(), "Student ID must not be null"); //ensure ID not null,safety check
        //go to the collection,create doc with ID and store entire student obj
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(studentId)
                .set(student);
        try {
            //wait until DB operation finishes
            future.get();
            return student.getStudentId(); // return the new student ID(for QR generation and API response)
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error saving student: " + e.getMessage());
            return null;
        }
    }

    // ─── GET by ID ──────────────────────────────────────────────────────────
    public Student getStudentById(String studentId) {
        Firestore db = FirestoreClient.getFirestore();
        String safeStudentId = java.util.Objects.requireNonNull(studentId, "studentId must not be null"); //validate ID
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(safeStudentId); //reference document
        ApiFuture<DocumentSnapshot> future = docRef.get(); //request to fetch doc/data, Firestore does not return data immediately
        try {
            DocumentSnapshot document = future.get(); //wait and fetch data
            if (document.exists()) {
                return document.toObject(Student.class); //convert to object(firestore to JSON to student obj)
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
            List<QueryDocumentSnapshot> documents = future.get().getDocuments(); //extract documents
            for (QueryDocumentSnapshot doc : documents) {
                students.add(doc.toObject(Student.class)); //convert each doc to object
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return students;
    }

    // ─── DELETE student from DB ─────────────────────────────────────────────────────
    public String deleteStudent(String studentId) {
        Firestore db = FirestoreClient.getFirestore();
        String safeStudentId = java.util.Objects.requireNonNull(studentId, "studentId must not be null");
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(safeStudentId).delete(); //delete doc
        try {
            future.get();
            return "Student " + studentId + " deleted successfully.";
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting student " + studentId + ": " + e.getMessage());
            return null;
        }
    }

    // ─── UPDATE STATUS ───────────────────────────────────────────────────
    public void updateStudentStatus(String studentId, String status) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            //Update currentStatus field in Firestore(called by attendanceservice and change the status.)
            db.collection(COLLECTION_NAME).document(java.util.Objects.requireNonNull(studentId)).update("currentStatus", status).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error updating student status: " + e.getMessage());
        }
    }

    // ─── GET Students BY STATUS ───────────────────────────────────────────────────
    public List<Student> getStudentsByStatus(String status) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            //Query Firestore
            QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                    .whereEqualTo("currentStatus", status)
                    .get().get();
            List<Student> students = new ArrayList<>();
            //Convert documents to Student objects
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                students.add(doc.toObject(Student.class));
            }
            return students;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching students by status: " + e.getMessage());
        }
    }

    // ─── GET Students BY PARENT EMAIL ──────────────────────────────────────────────
    public List<Student> getStudentsByParentEmail(String parentEmail) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            //Query Firestore by parent email
            QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                    .whereEqualTo("parentEmail", parentEmail)
                    .get().get();
            List<Student> students = new ArrayList<>();
            //Convert documents to Student objects.
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                students.add(doc.toObject(Student.class));
            }
            return students;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching students by parent email: " + e.getMessage());
        }
    }
}
//documentsnapshot
//students (collection)
// ├── STU-1234 (document)
// │      studentId: "STU-1234"
// │      studentName: "Kamal"
// │      parentEmail: "parent@gmail.com"
// │      currentStatus: "AT_HOME"
// ├── STU-XY98KL76

//DB logics only