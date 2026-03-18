package com.codex.saferidebackend.repository;

import com.codex.saferidebackend.model.Student;
import com.codex.saferidebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Custom query: Find all students belonging to a specific parent
    List<Student> findByParent(User parent);

    // Custom query: Find a student by their unique QR code
    Student findByQrcodeData(String qrcodeData);
}