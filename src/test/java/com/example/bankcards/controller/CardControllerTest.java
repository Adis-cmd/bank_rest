package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.AuthUserDetailsService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

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
    @WithMockUser(username = "qwerty@gmail.com")
    void cards_success() throws Exception {
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        when(cardService.cardsUser(null, "qwerty@gmail.com", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(cardDto)));

        mockMvc.perform(get("/api/cards")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void cards_withStatusFilter() throws Exception {
        when(cardService.cardsUser(eq("ACTIVE"), eq("qwerty@gmail.com"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/cards?status=ACTIVE")
                        .with(csrf()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void balance_success() throws Exception {
        when(cardService.getCardById(1L, "qwerty@gmail.com"))
                .thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/cards/1/balance")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void balance_cardNotFound_returns404() throws Exception {
        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).getCardById(99L, "qwerty@gmail.com");

        mockMvc.perform(get("/api/cards/99/balance")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void blockRequest_success() throws Exception {
        mockMvc.perform(put("/api/cards/1/block-request")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Запрос на блокировку карты отправлен"));

        verify(cardService, times(1)).requestBlockCard(1L, "qwerty@gmail.com");
    }
}
