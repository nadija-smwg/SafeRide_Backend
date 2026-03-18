package com.codex.saferidebackend.repository;

import com.codex.saferidebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // For Login: Find a user by email to check their password
    Optional<User> findByEmail(String email);

    // For Authorization: Find a user by their active token
    Optional<User> findByAuthToken(String authToken);
}
