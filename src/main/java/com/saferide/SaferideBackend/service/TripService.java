package com.saferide.SaferideBackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.model.TripSession;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TripService {

    private static final String COLLECTION_NAME = "trips";

    public String startTrip(TripSession session) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        // Saves the trip as a document.If the document with the same ID exists, it
        // overwrites it.
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COLLECTION_NAME)
                .document(java.util.Objects.requireNonNull(session.getTripId())).set(session);
        try {
            // Wait for completion and return timestamp
            return collectionsApiFuture.get().getUpdateTime().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error starting trip: " + e.getMessage());
        }
    }

    public void updateLocation(String tripId, Double lat, Double lon) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        // Get document reference for the trip
        DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME).document(tripId);
        try {
            // Firestore operation overwrites only these fields, not the entire document.
            docRef.update("currentLat", lat, "currentLong", lon, "lastUpdateTime", new Date()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error updating location: " + e.getMessage());
        }
    }

    public void endTrip(String tripId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME).document(tripId);
        try {
            docRef.update("status", "COMPLETED", "endTime", new Date(), "lastUpdateTime", new Date()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error ending trip: " + e.getMessage());
        }
    }

    public TripSession getActiveTripByDriver(String driverId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            // Query Firestore trips collection
            QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                    .whereEqualTo("driverId", driverId)
                    .whereEqualTo("status", "ONGOING")
                    .get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            // Convert document to TripSession object
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(TripSession.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching active trip: " + e.getMessage());
        }
        return null;
    }

    public TripSession getTripById(String tripId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        // Get document reference by ID
        DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME)
                .document(java.util.Objects.requireNonNull(tripId));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            // Fetch document
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                // Convert to TripSession object
                return document.toObject(TripSession.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching trip by ID: " + e.getMessage());
        }
        return null;
    }

    public String calculateETA(String studentId, String tripId) {
        // In a real application, this would use Google Maps Distance Matrix API
        // For now, we return a mock ETA based on active trip data
        TripSession session = getTripById(tripId);
        if (session == null || session.getCurrentLat() == null) {
            return "Calculation pending...";
        }
        return "Estimated 15 mins";
    }
}

// In a real app,we would integrate Google Maps Distance Matrix API to calculate
// ETA based on current location, route, and traffic.

// trips (collection)
// ├── TRIP-001 (document)
// │ tripId: "TRIP-001"
// │ driverId: "DRV-123"
// │ routeId: "ROUTE-01"
// │ startTime: 2026-03-29T07:30
// │ endTime: null
// │ status: "ONGOING"
// │ currentLat: 6.9271
// │ currentLong: 79.8612
// │ lastUpdateTime: 2026-03-29T07:35
// ├── TRIP-002
