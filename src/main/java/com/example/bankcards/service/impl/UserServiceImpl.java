package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthorityService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void deleteUser(Long userId) {
        User user = repository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Not found!")
        );
        repository.delete(user);
    }

    @Override
    public Page<UserDto> getAllUser(Pageable pageable) {
        Page<User> users = repository.findAll(pageable);

        return users.map(user -> UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .build());
    }


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



    @Override
    public User findById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!!!"));
    }


    @Override
    public User findUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!!!"));
    }

}
