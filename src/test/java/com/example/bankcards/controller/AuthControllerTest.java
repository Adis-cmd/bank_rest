package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthLoginDto;
import com.example.bankcards.dto.AuthRegisterDto;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.security.AuthUserDetailsService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @Test
    @WithMockUser
    void register_success() throws Exception {
        AuthRegisterDto dto = AuthRegisterDto.builder()
                .name("Тест")
                .surname("Тестов")
                .email("qwert@gmail.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).saveUser(any());
    }

    @Test
    @WithMockUser
    void register_emailAlreadyExists_returns409() throws Exception {
        AuthRegisterDto dto = AuthRegisterDto.builder()
                .name("Тест")
                .surname("Тестов")
                .email("qwert@gmail.com")
                .password("password123")
                .build();

        doThrow(new EmailAlreadyExistsException("qwert@gmail.com"))
                .when(userService).saveUser(any());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void register_invalidDto_returns400() throws Exception {
        AuthRegisterDto dto = AuthRegisterDto.builder().build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser
    void login_success() throws Exception {
        AuthLoginDto dto = AuthLoginDto.builder()
                .email("qwert@gmail.com")
                .password("password123")
                .build();

        when(jwtTokenUtils.generateToken("qwert@gmail.com")).thenReturn("test-token");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    @WithMockUser
    void login_badCredentials_returns401() throws Exception {
        AuthLoginDto dto = AuthLoginDto.builder()
                .email("qwert@gmail.com")
                .password("wrongpassword")
                .build();

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}