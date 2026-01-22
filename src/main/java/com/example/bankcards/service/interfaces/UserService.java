package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    void deleteUser(Long id);
    UserResponseDto updateUserLockStatus(Long id, boolean isLocked);
}