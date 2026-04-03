package com.saferide.SaferideBackend.controller;

import com.saferide.SaferideBackend.dto.DriverModeRequest;
import com.saferide.SaferideBackend.model.Student;
import com.saferide.SaferideBackend.model.TripSession;
import com.saferide.SaferideBackend.service.StudentService;
import com.saferide.SaferideBackend.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/start")
    public ResponseEntity<?> startTrip(@RequestBody TripSession session) {
        // Ensures driverId and routeId are provided
        if (session.getDriverId() == null || session.getRouteId() == null) {
            return ResponseEntity.badRequest().body("Driver ID and Route ID are required.");
        }

        // Check if there's already an active trip for this driver(Prevents starting a
        // new trip)
        TripSession activeTrip = tripService.getActiveTripByDriver(session.getDriverId());
        if (activeTrip != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Driver already has an ongoing trip: " + activeTrip.getTripId());
        }
        // Initialize trip session
        session.setTripId(UUID.randomUUID().toString());
        session.setStartTime(new Date());
        session.setStatus("ONGOING");
        session.setLastUpdateTime(new Date());

        String updateTime = tripService.startTrip(session); // Save to Firestore
        return ResponseEntity.ok("Trip started at " + updateTime + " with ID: " + session.getTripId());
    }

    @PostMapping("/mode")
    public ResponseEntity<?> toggleDriverMode(@RequestBody DriverModeRequest request) {
        if (request.getDriverId() == null) {
            return ResponseEntity.badRequest().body("Driver ID is required.");
        }

        TripSession activeTrip = tripService.getActiveTripByDriver(request.getDriverId());
        // If already ON,return current trip.If not ON,start new trip session
        if (request.isEnabled()) {
            // Turning mode ON
            if (activeTrip != null) {
                return ResponseEntity.ok(activeTrip); // Already ON, return current trip session
            }
            if (request.getRouteId() == null) {
                return ResponseEntity.badRequest().body("Route ID is required to start driver mode.");
            }

            // Start new trip session
            TripSession session = new TripSession();
            session.setTripId(UUID.randomUUID().toString());
            session.setDriverId(request.getDriverId());
            session.setRouteId(request.getRouteId());
            session.setStartTime(new Date());
            session.setStatus("ONGOING");
            session.setLastUpdateTime(new Date());

            tripService.startTrip(session);
            return ResponseEntity.ok(session);
        } else {
            // Turning mode OFF
            if (activeTrip == null) {
                return ResponseEntity.ok("Driver mode was already OFF (no active trip found).");
            }

            // Safety Check: Verify all students are dropped off
            List<Student> studentsStillOnBus = studentService.getStudentsByStatus("PICKED_UP");
            if (!studentsStillOnBus.isEmpty()) {
                String names = studentsStillOnBus.stream()
                        .map(Student::getStudentName)
                        .collect(Collectors.joining(", "));
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                        .body("Cannot turn OFF driver mode. These students are not dropped off: " + names);
            }

            tripService.endTrip(activeTrip.getTripId()); // all clear,end trip
            return ResponseEntity.ok("Driver mode turned OFF. Trip session ended.");
        }
    }

    @PostMapping("/{tripId}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String tripId, @RequestParam Double lat,
            @RequestParam Double lon) {
        tripService.updateLocation(tripId, lat, lon);// Update trip’s current location in Firestore
        return ResponseEntity.ok("Location updated.");
    }

    @PostMapping("/{tripId}/end")
    public ResponseEntity<?> endTrip(@PathVariable String tripId) {
        // Verification: Check if all students picked up are dropped off
        List<Student> studentsStillOnBus = studentService.getStudentsByStatus("PICKED_UP");

        // For simplicity now, we check all who are "PICKED_UP"
        // If students still on bus then block trip end
        if (!studentsStillOnBus.isEmpty()) {
            String names = studentsStillOnBus.stream().map(Student::getStudentName).collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body("Cannot end trip. These students are not dropped off: " + names);
        }
        // If clear → end trip in Firestore
        tripService.endTrip(tripId);
        return ResponseEntity.ok("Trip ended successfully.");
    }

    @GetMapping("/active/{driverId}")
    public ResponseEntity<?> getActiveTrip(@PathVariable String driverId) {// Fetches active trip session for a driver
        TripSession session = tripService.getActiveTripByDriver(driverId);
        if (session != null) {
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active trip found for driver: " + driverId);
    }

    @GetMapping("/mode/{driverId}")
    // Checks if driver has an active trip(enabled = true/false)
    // Returns active trip details if driver mode is ON
    public ResponseEntity<?> getDriverMode(@PathVariable String driverId) {
        TripSession activeTrip = tripService.getActiveTripByDriver(driverId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("driverId", driverId);
        response.put("enabled", activeTrip != null);
        if (activeTrip != null) {
            response.put("activeTrip", activeTrip);
        }
        return ResponseEntity.ok(response);
    }
}
