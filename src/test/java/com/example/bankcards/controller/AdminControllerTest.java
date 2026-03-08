package com.example.bankcards.controller;


import com.example.bankcards.dto.AdminCardResponse;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.AuthUserDetailsService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_success() throws Exception {
        Page<AdminCardResponse> page = new PageImpl<>(List.of(
                AdminCardResponse.builder()
                        .name("Ivan")
                        .surname("Ivanov")
                        .maskedCardNumber("**** **** **** 1234")
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(1000))
                        .build()
        ));

        when(cardService.getAllCards(null, PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_withStatusFilter_success() throws Exception {
        when(cardService.getAllCards(eq("ACTIVE"), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/cards")
                        .param("status", "ACTIVE")
                        .with(csrf()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(
                UserDto.builder()
                        .id(1L)
                        .name("Ivan")
                        .surname("Ivanov")
                        .build()
        ));

        when(userService.getAllUser(any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ivan"))
                .andExpect(jsonPath("$.content[0].surname").value("Ivanov"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно удалён"));

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/admin/users/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardsByUserId_success() throws Exception {
        Page<AdminCardResponse> page = new PageImpl<>(List.of(
                AdminCardResponse.builder()
                        .name("Ivan")
                        .surname("Ivanov")
                        .maskedCardNumber("**** **** **** 5678")
                        .status(CardStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(500))
                        .build()
        ));

        when(cardService.getCardsByUserId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users/1/cards").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ivan"))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 5678"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardsByUserId_userNotFound_returns404() throws Exception {
        when(cardService.getCardsByUserId(eq(99L), any()))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/admin/users/99/cards")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_success() throws Exception {
        CardCreateDto dto = CardCreateDto.builder()
                .userId(1L)
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


    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_success() throws Exception {
        mockMvc.perform(put("/api/admin/cards/1/activate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта успешно активирована"));

        verify(cardService, times(1)).activeCard(1L);
    }


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