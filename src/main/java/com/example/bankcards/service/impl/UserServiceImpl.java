package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthorityService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void saveUser(AuthRegisterDto dto) {

        if (repository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        User user = User.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .enabled(true)
                .authority(authorityService.findAuthorityByName("ROLE_USER"))
                .build();

        repository.save(user);
    }

}
