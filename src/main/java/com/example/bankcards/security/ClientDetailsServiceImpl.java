package com.example.bankcards.security;

import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientDetailsServiceImpl implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public Client loadUserByUsername(String username) throws UsernameNotFoundException {

        return  clientRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


    }

}