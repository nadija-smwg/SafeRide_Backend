package com.saferide.SaferideBackend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.dto.StudentRequest;
import com.saferide.SaferideBackend.models.ScheduleItem;
import com.saferide.SaferideBackend.models.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service("parentStudentService")
public class ParentStudentService {
    public Student addStudent(String parentId, StudentRequest request) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        String studentId = UUID.randomUUID().toString();
        
        Student student = new Student();
        student.setId(studentId);
        student.setParentId(parentId);
        student.setFullName(request.getFullName());
        student.setSchoolName(request.getSchoolName());
        student.setAge(request.getAge());
        student.setHomeLat(request.getHomeLat());
        student.setHomeLng(request.getHomeLng());
        student.setSchoolLat(request.getSchoolLat());
        student.setSchoolLng(request.getSchoolLng());
        student.setHomeAddress(request.getHomeAddress());
        student.setSchoolAddress(request.getSchoolAddress());
        
        // initialize default schedule with Home
        String parentHome = getParentHomeAddress(parentId, db);
        List<ScheduleItem> defaultSchedule = new ArrayList<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (String day : days) {
            defaultSchedule.add(new ScheduleItem(day, parentHome, parentHome, true));
        }
        student.setWeeklySchedule(defaultSchedule);
        
        db.collection("students").document(studentId).set(student).get();
        return student;
    }
    
    private String getParentHomeAddress(String parentId, Firestore db) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("parents").document(parentId).get().get();
        if (doc.exists() && doc.getString("homeAddress") != null) {
            return doc.getString("homeAddress");
        }
        return "Home";
    }

    public List<Student> getStudentsByParent(String parentId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("students").whereEqualTo("parentId", parentId).get();
        List<Student> students = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            students.add(document.toObject(Student.class));
        }
        return students;
    }

    public Student getStudentById(String studentId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("students").document(studentId).get().get();
        if (doc.exists()) {
            return doc.toObject(Student.class);
        }
        throw new RuntimeException("Student not found");
    }

    public Student updateStudentBio(String studentId, StudentRequest request) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("students").document(studentId);
        
        docRef.update("fullName", request.getFullName(),
                      "schoolName", request.getSchoolName(),
                      "age", request.getAge()).get();
                      
        return getStudentById(studentId);
    }

    public Student updateStudentSchedule(String studentId, List<ScheduleItem> schedule) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("students").document(studentId);
        
        docRef.update("weeklySchedule", schedule).get();
        return getStudentById(studentId);
    }

    public java.util.Map<String, Object> getDriverDetails(String driverId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("drivers").document(driverId).get().get();
        if (doc.exists()) {
            return doc.getData();
        }
        throw new RuntimeException("Driver not found");
    }
}
