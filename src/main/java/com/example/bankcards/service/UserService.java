package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRegisterDto;

public interface UserService {
    void saveUser(AuthRegisterDto dto);
}
