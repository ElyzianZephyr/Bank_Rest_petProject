package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // Stores BCrypt hash

    @Column(nullable = false, length = 32)
    private String role; // e.g., "ROLE_USER", "ROLE_ADMIN"

    // One-to-Many relationship: Client is the "inverse" side
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    public Client(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}