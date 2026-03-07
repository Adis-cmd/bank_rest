package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface TransactionService {
    void transfer(Card from, Card to, BigDecimal amount);

    @Transactional
    void deleteTransaction(Long fromCardId, Long toCardId);
}
