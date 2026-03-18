package com.codex.saferidebackend.config;

import com.codex.saferidebackend.model.Student;
import com.codex.saferidebackend.model.User;
import com.codex.saferidebackend.repository.StudentRepository;
import com.codex.saferidebackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, StudentRepository studentRepo) {
        return args -> {
            // Check if the user already exists first!
            if (userRepo.findByEmail("parent@example.com").isEmpty()) {
                User parent = new User();
                parent.setUsername("Akira_parent");
                parent.setEmail("parent@example.com");
                parent.setPassword("1234");
                parent.setRole("PARENT");
                userRepo.save(parent);

                Student student = new Student();
                student.setStudentName("Little Akira");
                student.setQrcodeData("SAFE-123");
                student.setParent(parent);
                studentRepo.save(student);

                System.out.println("--- New Test Data Loaded! ---");
            } else {
                System.out.println("--- Test Data already exists in MySQL, skipping init ---");
            }
        };
    }
}