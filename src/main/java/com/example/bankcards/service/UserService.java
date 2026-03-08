package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void deleteUser(Long userId);

    Page<UserDto> getAllUser(Pageable pageable);

    void saveUser(AuthRegisterDto dto);

    User findById(Long userId);

    User findUserByEmail(String email);
}
