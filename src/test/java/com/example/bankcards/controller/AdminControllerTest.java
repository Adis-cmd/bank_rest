package com.example.bankcards.controller;


import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.AuthUserDetailsService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;


    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;



    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_success() throws Exception {
        CardCreateDto dto = CardCreateDto.builder()
                .userId(1L)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(cardService, times(1)).createCard(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_invalidDto_returns400() throws Exception {
        CardCreateDto dto = CardCreateDto.builder().build(); // пустой dto

        mockMvc.perform(post("/api/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ===== blockCard =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_success() throws Exception {
        mockMvc.perform(put("/api/admin/cards/1/block")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта успешно заблокирована"));

        verify(cardService, times(1)).blockCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_notFound_returns404() throws Exception {
        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).blockCard(99L);

        mockMvc.perform(put("/api/admin/cards/99/block")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ===== activateCard =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_success() throws Exception {
        mockMvc.perform(put("/api/admin/cards/1/activate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта успешно активирована"));

        verify(cardService, times(1)).activeCard(1L);
    }

    // ===== deleteCard =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_success() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта успешно удалена"));

        verify(cardService, times(1)).deleteCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_activeCard_returns400() throws Exception {
        doThrow(new CardNotActiveException("Нельзя удалить активную карту"))
                .when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/api/admin/cards/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}