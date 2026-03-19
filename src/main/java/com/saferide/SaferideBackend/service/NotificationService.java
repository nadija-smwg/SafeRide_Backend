package com.saferide.SaferideBackend.service;

import com.saferide.SaferideBackend.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired(required = false) // required=false so app starts even if email is not configured yet
    private JavaMailSender mailSender;

    public void sendPickupNotification(Student student) {
        String subject = "SafeRide: Pickup Confirmed for " + student.getStudentName();
        String message = "Hello,\n\n" + student.getStudentName() +
                " has boarded the bus safely at " + LocalDateTime.now() + ".\n\nSafeRide System";
        sendEmail(student.getParentEmail(), subject, message);
    }

    public void sendDropoffNotification(Student student) {
        String subject = "SafeRide: Dropoff Confirmed for " + student.getStudentName();
        String message = "Hello,\n\n" + student.getStudentName() +
                " has been dropped off safely at " + LocalDateTime.now() + ".\n\nSafeRide System";
        sendEmail(student.getParentEmail(), subject, message);
    }

    private void sendEmail(String to, String subject, String text) {
        System.out.println("Preparing to send Email to: " + to + " | Subject: " + subject);

        if (mailSender == null) {
            System.out.println("MailSender is not configured yet. Skipping actual email send.");
            System.out.println("Message: " + text);
            return;
        }

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(text);
            mailSender.send(email);
            System.out.println("Email successfully sent.");
        } catch (Exception e) {
            System.err.println("Failed to send email. Check SMTP configuration in application.properties: " + e.getMessage());
        }
    }
}
