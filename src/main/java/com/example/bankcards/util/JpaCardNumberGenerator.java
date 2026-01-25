package com.example.bankcards.util;

import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.interfaces.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaCardNumberGenerator implements CardNumberGenerator {

    private final CardRepository cardRepository;

    @Override
    public String generate() {
        return String.format("%016d", cardRepository.getNextCardNumber());
    }
}
