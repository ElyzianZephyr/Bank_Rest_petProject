package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // --- Existing Methods ---

    // Find all cards by Owner (using the owner's ID)
    List<Card> findAllByOwnerId(Long ownerId);

    // Securely find a card by ID AND Owner
    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);

    // --- Step 1: Pagination & Search Updates ---

    /**
     * Retrieve a paged list of cards for a specific owner.
     *
     * @param ownerId  The ID of the card owner.
     * @param pageable Pagination information (page number, size, sort).
     * @return A page of cards.
     */
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Search for cards by owner and a partial card number fragment.
     * <p>
     * Note: Since the 'cardNumber' column is encrypted with a random IV (AES-GCM),
     * a standard database 'LIKE' query cannot effectively match plaintext fragments
     * against the stored ciphertext. This method is defined to meet the interface
     * requirements but would require deterministic encryption or a hash column
     * to function as expected in a production environment.
     *
     * @param ownerId    The ID of the card owner.
     * @param cardNumber The partial card number to search for.
     * @param pageable   Pagination information.
     * @return A page of matching cards.
     */
    Page<Card> findAllByOwnerIdAndCardNumberContaining(Long ownerId, String cardNumber, Pageable pageable);

    @Query(value = "select nextval('cards_number_seq') ",nativeQuery = true)
    Long getNextCardNumber();
}