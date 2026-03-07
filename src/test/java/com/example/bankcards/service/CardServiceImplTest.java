package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserService userService;
    @Mock private CardEncryptionUtil cardEncryptionUtil;
    @Mock private TransactionService transactionService;
    @InjectMocks private CardServiceImpl cardService;


    @Test
    void createCard_success() {
        User user = User.builder().id(1L).build();
        CardCreateDto dto = CardCreateDto.builder()
                .userId(1L)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        when(userService.findById(1L)).thenReturn(user);
        when(cardEncryptionUtil.encrypt(any())).thenReturn("encryptedNumber");
        when(cardRepository.existsByCardNumber(any())).thenReturn(false);

        cardService.createCard(dto);

        verify(cardRepository, times(1)).save(any(Card.class));
    }


    @Test
    void blockCard_success() {
        Card card = Card.builder().id(1L).status(CardStatus.BLOCK_REQUESTED).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_notBlockRequested_throwsException() {
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotActiveException.class, () -> cardService.blockCard(1L));
    }


    @Test
    void activeCard_success() {
        Card card = Card.builder().id(1L).status(CardStatus.BLOCKED).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.activeCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activeCard_alreadyActive_throwsException() {
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotActiveException.class, () -> cardService.activeCard(1L));
    }


    @Test
    void deleteCard_success() {
        Card card = Card.builder().id(1L).status(CardStatus.BLOCKED).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(transactionService).deleteTransaction(1L, 1L);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_activeCard_throwsException() {
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardNotActiveException.class, () -> cardService.deleteCard(1L));
    }


    @Test
    void transferCard_success() {
        User user = User.builder().id(1L).build();
        Card fromCard = Card.builder().id(1L).owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();
        Card toCard = Card.builder().id(2L).owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        TransferDto dto = TransferDto.builder()
                .fromCard(1L).toCard(2L)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(userService.findUserByEmail("qwerty@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        cardService.transferCard("qwerty@gmail.com", dto);

        verify(transactionService).transfer(fromCard, toCard, BigDecimal.valueOf(100));
    }

    @Test
    void transferCard_insufficientFunds_throwsException() {
        User user = User.builder().id(1L).build();
        Card fromCard = Card.builder().id(1L).owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(50))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();
        Card toCard = Card.builder().id(2L).owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        TransferDto dto = TransferDto.builder()
                .fromCard(1L).toCard(2L)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(userService.findUserByEmail("qwerty@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class,
                () -> cardService.transferCard("qwerty@gmail.com", dto));
    }

    @Test
    void transferCard_cardNotBelongsToUser_throwsException() {
        User user = User.builder().id(1L).build();
        User otherUser = User.builder().id(2L).build();
        Card fromCard = Card.builder().id(1L).owner(otherUser)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();
        Card toCard = Card.builder().id(2L).owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .build();

        TransferDto dto = TransferDto.builder()
                .fromCard(1L).toCard(2L)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(userService.findUserByEmail("qwerty@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(AccessDeniedException.class,
                () -> cardService.transferCard("qwerty@gmail.com", dto));
    }
}