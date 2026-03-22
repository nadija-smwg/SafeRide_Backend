package com.saferide.SaferideBackend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.saferide.SaferideBackend.model.Student;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class NotificationService {

    //If SMTP is set,emails work
    @Autowired(required = false) //Spring injects email service,required=false so app starts even if email is not configured yet
    private JavaMailSender mailSender;

    // ─── Pickup Notification ────────────────────────────────────────────────
    public void sendPickupNotification(Student student) {
        String subject = "SafeRide: Pickup Confirmed for " + student.getStudentName();
        String body = "Hello,\n\n" + student.getStudentName() +
                " has boarded the bus safely at " + LocalDateTime.now() + ".\n\nSafeRide System";
        
        //send Email
        sendSimpleEmail(student.getParentEmail(), subject, body);

        //send Push Notification
        if (student.getParentFcmToken() != null && !student.getParentFcmToken().isEmpty()) {
            sendPushNotification(student.getParentFcmToken(), subject, body);
        }
    }

    // ─── Dropoff Notification ───────────────────────────────────────────────
    public void sendDropoffNotification(Student student) {
        String subject = "SafeRide: Dropoff Confirmed for " + student.getStudentName();
        String body = "Hello,\n\n" + student.getStudentName() +
                " has been dropped off safely at " + LocalDateTime.now() + ".\n\nSafeRide System";

        // Email
        sendSimpleEmail(student.getParentEmail(), subject, body);

        // Push Notification
        if (student.getParentFcmToken() != null && !student.getParentFcmToken().isEmpty()) {
            sendPushNotification(student.getParentFcmToken(), subject, body);
        }
    }

    // ─── QR Code Email (sent when student is registered) ───────────────────
    public void sendQRCodeToParent(Student student, String qrCodeBase64) {
        System.out.println("Preparing QR code email for: " + student.getParentEmail());
        //prevents crash if SMTP not set
        if (mailSender == null) {
            System.out.println("MailSender not configured. Skipping QR email.");
            return;
        }

        try {
            //create MIME email(supports for text,HTML,attachments and images)
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(student.getParentEmail());
            helper.setSubject("SafeRide: QR Code for " + student.getStudentName());
            helper.setText(
                "Hello,\n\n" +
                "Welcome to SafeRide! Your child " + student.getStudentName() + " has been registered.\n\n" +
                "Please find the QR code attached. Print it out and attach it to your child's bag or ID card.\n" +
                "The driver will scan this code every time your child gets on or off the bus.\n\n" +
                "Student ID: " + student.getStudentId() + "\n\n" +
                "SafeRide System"
            );

            // Attach QR code image
            byte[] qrBytes = Base64.getDecoder().decode(qrCodeBase64); //Convert Base64 to image bytes...qrCodeBase64-String representation of the QR image(encoded data)
            helper.addAttachment(
                student.getStudentName().replace(" ", "_") + "_QRCode.png",
                new ByteArrayDataSource(qrBytes, "image/png") //file content
            );

            mailSender.send(mimeMessage);
            System.out.println("QR code email sent successfully to " + student.getParentEmail());

        } catch (Exception e) {
            System.err.println("Failed to send QR code email: " + e.getMessage());
        }
    }


    // ─── Firebase Push Notification (FCM) ───────────────────────────────────
    public void sendPushNotification(String token, String title, String body) {
        System.out.println("Sending Push Notification to: " + token); //helps to check if method is called and debug token issues (prints in console)

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build(); //start building a Firebase message to specific phone

            String response = FirebaseMessaging.getInstance().send(message); //Returns response ID,FB sends notification to device
            System.out.println("Firebase push notification sent: " + response);

        } catch (Exception e) { //invalid token,network issue,FB error
            System.err.println("Failed to send Firebase push notification: " + e.getMessage());
        }
    }

    // ─── Internal: Simple text email ───────────────────────────────────────
    private void sendSimpleEmail(String to, String subject, String text) {
        System.out.println("Sending email to: " + to + " | Subject: " + subject);

        if (mailSender == null) {
            System.out.println("MailSender not configured. Skipping email.");
            System.out.println("Message: " + text); //prevents crash,still  shows msg in console
            return;
        }

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(text);
            mailSender.send(email);
            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            System.err.println("Failed to send email. Check SMTP config: " + e.getMessage());
        }
    }
}
