package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.enums.TransactionStatus;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;

    @Override
    @Transactional
    public void transfer(Card from, Card to, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .fromCard(from)
                .toCard(to)
                .amount(amount)
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatus.SUCCESS)
                .build();
        repository.save(transaction);
    }



    @Transactional
    @Override
    public void deleteTransaction(Long fromCardId, Long toCardId) {
        repository.deleteByFromCardIdOrToCardId(fromCardId, toCardId);
    }
}
