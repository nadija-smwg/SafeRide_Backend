package com.saferide.SaferideBackend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.models.Student;
import com.saferide.SaferideBackend.models.ScheduleItem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class StudentStatusResetTask {

    // Runs every day at 11:59 PM (23:59:00 server time)
    @Scheduled(cron = "0 59 23 * * ?")
    public void resetAllStudentStatuses() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("students").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            WriteBatch batch = db.batch();
            
            // The reset stages the default status for the start of the next day:
            String tomorrowName = LocalDate.now().plusDays(1).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            for (QueryDocumentSnapshot document : documents) {
                String newStatus = "AT_HOME"; 
                Student student = document.toObject(Student.class);
                
                if (student.getWeeklySchedule() != null) {
                    for (ScheduleItem item : student.getWeeklySchedule()) {
                        if (tomorrowName.equalsIgnoreCase(item.getDay())) {
                            // If a child is NOT getting picked up in the morning,
                            // but IS getting picked up in the evening, they conceptually start the day already AT_SCHOOL.
                            if (!item.isNeedMorningPickup() && item.isNeedEveningPickup()) {
                                newStatus = "IN_SCHOOL";
                            }
                            break;
                        }
                    }
                }
                
                batch.update(document.getReference(), "status", newStatus);
            }

            // Commit the batch to firestore
            batch.commit().get();
            System.out.println("Scheduler: Successfully reset all students based on " + tomorrowName + " transit settings.");
        } catch (Exception e) {
            System.err.println("Scheduler Error: Failed to reset student statuses: " + e.getMessage());
        }
    }
}
