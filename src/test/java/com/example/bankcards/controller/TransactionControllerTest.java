package com.example.bankcards.controller;


import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.exception.InsufficientFundsException;
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

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

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
    void transfer_success() throws Exception {
        TransferDto dto = TransferDto.builder()
                .fromCard(1L)
                .toCard(2L)
                .amount(BigDecimal.valueOf(100))
                .build();

        mockMvc.perform(post("/api/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(cardService, times(1)).transferCard(eq("qwerty@gmail.com"), any());
    }

    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void transfer_insufficientFunds_returns400() throws Exception {
        TransferDto dto = TransferDto.builder()
                .fromCard(1L)
                .toCard(2L)
                .amount(BigDecimal.valueOf(100))
                .build();

        doThrow(new InsufficientFundsException("Недостаточно средств"))
                .when(cardService).transferCard(any(), any());

        mockMvc.perform(post("/api/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "qwerty@gmail.com")
    void transfer_invalidDto_returns400() throws Exception {
        TransferDto dto = TransferDto.builder().build();

        mockMvc.perform(post("/api/transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}