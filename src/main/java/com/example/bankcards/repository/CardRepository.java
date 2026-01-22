package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // Find all cards by Owner (using the owner's ID)
    List<Card> findAllByOwnerId(Long ownerId);


    // Optional: Securely find a card by ID AND Owner
    // (Ensures a user can only perform actions on their own cards)
    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);

    // Note: Standard findById(Long id) is already provided by JpaRepository
}