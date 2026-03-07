package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.entity.User;

public interface UserService {
    void saveUser(AuthRegisterDto dto);

    User findById(Long userId);

    User findUserByEmail(String email);
}
