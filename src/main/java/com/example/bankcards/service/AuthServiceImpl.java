package com.example.bankcards.service;

import com.example.bankcards.dto.requests.AuthRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.repository.ClientRepository;
import com.example.bankcards.service.interfaces.AuthService;
import com.example.bankcards.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponseDto register(AuthRequestDto request) {
        if (clientRepository.existsByUsername(request.username())) {
            throw new RestException("Username is already taken", HttpStatus.CONFLICT);
        }

        Client client = new Client();
        client.setUsername(request.username());
        client.setPassword(passwordEncoder.encode(request.password()));
        client.setRole(Role.ROLE_USER);


        clientRepository.save(client);


        return login(request);
    }

    @Override
    public AuthResponseDto login(AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );


        SecurityContextHolder.getContext().setAuthentication(authentication);


        String token = jwtUtils.generateToken(authentication);


        return new AuthResponseDto(token);
    }
}