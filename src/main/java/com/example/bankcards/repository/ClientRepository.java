package com.example.bankcards.repository;

import com.example.bankcards.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Find by username (used for Auth/Login)
    Optional<Client> findByUsername(String username);

    // Check if user exists (used for Registration validation)
    boolean existsByUsername(String username);
}