package com.example.bankcards.service;

import com.example.bankcards.dto.AdminCardResponse;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {
    Page<AdminCardResponse> getCardsByUserId(Long userId, Pageable pageable);

    Page<AdminCardResponse> getAllCards(String status, Pageable pageable);

    void requestBlockCard(Long cardId, String email);

    void blockCard(Long cardId);

    void activeCard(Long cardId);

    BigDecimal getCardById(Long cardId, String email);

    void transferCard(String email, TransferDto dto);

    void createCard(CardCreateDto dto);

    Page<CardDto> cardsUser(String status, String email, Pageable pageable);

    void deleteCard(Long cardId);
}
