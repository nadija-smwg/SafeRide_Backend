package com.saferide.SaferideBackend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import com.saferide.SaferideBackend.dto.AuthResponse;
import com.saferide.SaferideBackend.dto.LoginRequest;
import com.saferide.SaferideBackend.dto.RegisterRequest;
import com.saferide.SaferideBackend.models.User;
import com.saferide.SaferideBackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Value("${firebase.api.key:}")
    private String firebaseApiKey;

    private final WebClient webClient = WebClient.create();

    public AuthResponse register(RegisterRequest request) throws Exception {
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new Exception("Passwords do not match.");
        }

        // Create user in Firebase Auth
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setEmailVerified(false);
        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String uid = userRecord.getUid();

        // Save role in Firestore for main users table
        Firestore db = FirestoreClient.getFirestore();
        User user = new User(uid, request.getUsername(), request.getEmail(), request.getRole());
        ApiFuture<WriteResult> result = db.collection("users").document(uid).set(user);
        result.get(); // wait for save

        // Create specific profiles
        String role = request.getRole();
        if (role != null && (role.equalsIgnoreCase("Driver") || role.equalsIgnoreCase("ROLE_DRIVER"))) {
            if (request.getLicenseNumber() == null || request.getVehicleNumber() == null) {
                FirebaseAuth.getInstance().deleteUser(uid);
                db.collection("users").document(uid).delete();
                throw new Exception("Driver registration requires licenseNumber and vehicleNumber.");
            }
            Map<String, Object> driverData = new HashMap<>();
            driverData.put("fullName", request.getFullName());
            driverData.put("licenseNumber", request.getLicenseNumber());
            driverData.put("vehicleNumber", request.getVehicleNumber());
            driverData.put("phoneNumber", request.getPhoneNumber());
            db.collection("drivers").document(uid).set(driverData).get();
        } else if (role != null && (role.equalsIgnoreCase("Parent") || role.equalsIgnoreCase("ROLE_PARENT") || role.equalsIgnoreCase("Student") || role.equalsIgnoreCase("ROLE_STUDENT"))) {
            // Note: The prompt explicitly asked to create 'parents' collection for 'Parent' role. 
            // In earlier examples user used 'Student', I am applying 'parents' generic profile mapping
            if (role.equalsIgnoreCase("Parent") || role.equalsIgnoreCase("ROLE_PARENT")) {
                if (request.getHomeAddress() == null) {
                    FirebaseAuth.getInstance().deleteUser(uid);
                    db.collection("users").document(uid).delete();
                    throw new Exception("Parent registration requires a home address.");
                }
                Map<String, Object> parentData = new HashMap<>();
                parentData.put("fullName", request.getFullName());
                parentData.put("homeAddress", request.getHomeAddress());
                parentData.put("phoneNumber", request.getPhoneNumber());
                db.collection("parents").document(uid).set(parentData).get();
            }
        }

        try {
            // Generate email verification link
            String link = FirebaseAuth.getInstance().generateEmailVerificationLink(request.getEmail());
            emailService.sendEmail(request.getEmail(), "Verify your email", "Click here to verify: " + link);
        } catch (Exception e) {
            System.err.println("Warning: Failed to send verification email. " + e.getMessage());
        }

        return new AuthResponse("User registered successfully. Please check your email to verify.");
    }

    public AuthResponse login(LoginRequest request) throws Exception {
        if (firebaseApiKey == null || firebaseApiKey.isEmpty()) {
            throw new Exception("Firebase API Key is missing in configuration. Cannot perform backend login.");
        }

        String identityToolkitUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                + firebaseApiKey;
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        body.put("returnSecureToken", true);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(identityToolkitUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (response == null || !response.containsKey("localId")) {
                throw new Exception("Failed to retrieve user ID from authentication provider.");
            }

            String uid = (String) response.get("localId");

            // Check email verification via Admin SDK
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            if (!userRecord.isEmailVerified()) {
                throw new Exception("Email is not verified. Please verify your email first.");
            }
            // Get role from Firestore
            Firestore db = FirestoreClient.getFirestore();
            com.google.cloud.firestore.DocumentSnapshot document = db.collection("users").document(uid).get().get();
            if (!document.exists()) {
                throw new Exception("User role not found in Firestore Database.");
            }
            User user = document.toObject(User.class);
            if (user == null || user.getRole() == null) {
                throw new Exception("User role is missing or invalid in Firestore.");
            }

            if (!user.getRole().equals(request.getExpectedRole())) {
                throw new Exception("Unauthorized: This account is not a " + request.getExpectedRole());
            }

            // Generate backend JWT which frontend uses to determine dashboard
            String role = user.getRole();
            String token = jwtUtil.generateToken(user.getEmail(), role, uid);

            return new AuthResponse(token, uid, user.getEmail(), role, "Login successful");
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new Exception("Invalid credentials");
        }
    }

    public String forgotPassword(String email) throws Exception {
        try {
            // Generate the reset link from Firebase
            String resetLink = FirebaseAuth.getInstance().generatePasswordResetLink(email);

            // Send the link via your working emailService
            emailService.sendEmail(
                    email,
                    "Safe Ride - Reset Your Password",
                    "Hello! Use the link below to reset your Safe Ride password:\n\n" + resetLink);

            return "Password reset email sent successfully!";
        } catch (FirebaseAuthException e) {
            // If the email isn't in Firebase, this "Throws" the error back to the
            // controller
            throw new Exception("User not found: " + e.getMessage());
        } catch (Exception e) {
            // "Catcher" for any SMTP/Mail errors
            throw new Exception("Mail server error: " + e.getMessage());
        }
    }
}
