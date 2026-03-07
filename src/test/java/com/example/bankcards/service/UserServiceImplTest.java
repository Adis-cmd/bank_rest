package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.entity.Authority;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void saveUser_success() {
        AuthRegisterDto dto = AuthRegisterDto.builder()
                .name("Тест")
                .surname("Тестов")
                .email("qwerty@gmail.com")
                .password("password123")
                .build();

        Authority authority = new Authority();
        authority.setName("ROLE_USER");

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(authorityService.findAuthorityByName("ROLE_USER")).thenReturn(authority);

        userService.saveUser(dto);

        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void saveUser_emailAlreadyExists_throwsException() {
        AuthRegisterDto dto = AuthRegisterDto.builder()
                .name("Тест")
                .surname("Тестов")
                .email("qwerty@gmail.com")
                .password("password123")
                .build();

        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.saveUser(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void findById_success() {
        User user = User.builder()
                .id(1L)
                .email("qwerty@gmail.com")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("qwerty@gmail.com", result.getEmail());
    }

    @Test
    void findById_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    void findUserByEmail_success() {
        User user = User.builder()
                .email("qwerty@gmail.com")
                .build();

        when(repository.findByEmail("qwerty@gmail.com")).thenReturn(Optional.of(user));

        User result = userService.findUserByEmail("qwerty@gmail.com");

        assertEquals("qwerty@gmail.com", result.getEmail());
    }

    @Test
    void findUserByEmail_notFound_throwsException() {
        when(repository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findUserByEmail("notfound@gmail.com"));
    }
}