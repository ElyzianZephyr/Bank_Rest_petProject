package com.example.bankcards.service;

import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Client;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.repository.ClientRepository;
import com.example.bankcards.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return clientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RestException("User not found", HttpStatus.NOT_FOUND));
        return toDto(client);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RestException("User not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserLockStatus(Long id, boolean isLocked) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RestException("User not found", HttpStatus.NOT_FOUND));

        client.setLocked(isLocked);
        return toDto(clientRepository.save(client));
    }

    private UserResponseDto toDto(Client client) {
        return new UserResponseDto(
                client.getId(),
                client.getUsername(),
                client.getRole(),
                client.isLocked()
        );
    }
}