package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.TransactionStatus;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void transfer_success() {
        Card from = Card.builder().id(1L).build();
        Card to = Card.builder().id(2L).build();
        BigDecimal amount = BigDecimal.valueOf(100);

        transactionService.transfer(from, to, amount);

        verify(repository, times(1)).save(argThat(t ->
                t.getFromCard().equals(from) &&
                        t.getToCard().equals(to) &&
                        t.getAmount().equals(amount) &&
                        t.getStatus() == TransactionStatus.SUCCESS
        ));
    }

    @Test
    void deleteTransaction_success() {
        transactionService.deleteTransaction(1L, 1L);

        verify(repository, times(1))
                .deleteByFromCardIdOrToCardId(1L, 1L);
    }
}
