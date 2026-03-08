package com.example.bankcards.service;

import com.example.bankcards.dto.AdminCardResponse;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Authority;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardStatusException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardService, "maxTransferAmount", BigDecimal.valueOf(500000));
        ReflectionTestUtils.setField(cardService, "cardExpirationYears", 3);
    }


    @Test
    void createCard_success() {

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        User user = User.builder()
                .id(1L)
                .authority(authority)
                .build();
        CardCreateDto dto = CardCreateDto.builder()
                .userId(1L)
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

    @Test
    void getCardsByUserId_success() {
        User user = User.builder().id(1L).name("Тест").surname("Тестов").build();
        Card card = Card.builder()
                .id(1L)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .cardNumber("encryptedNumber")
                .build();

        when(userService.findById(1L)).thenReturn(user);
        when(cardRepository.findAllByOwnerId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardEncryptionUtil.decrypt("encryptedNumber")).thenReturn("1234567890121234");
        when(cardEncryptionUtil.mask("1234567890121234")).thenReturn("**** **** **** 1234");

        Page<AdminCardResponse> result = cardService.getCardsByUserId(1L, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("**** **** **** 1234", result.getContent().get(0).getMaskedCardNumber());
        assertEquals("Тест", result.getContent().get(0).getName());
    }

    @Test
    void getAllCards_success() {
        User user = User.builder().id(1L).name("Тест").surname("Тестов").build();
        Card card = Card.builder()
                .id(1L)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .cardNumber("encryptedNumber")
                .build();

        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardEncryptionUtil.decrypt("encryptedNumber")).thenReturn("1234567890121234");
        when(cardEncryptionUtil.mask("1234567890121234")).thenReturn("**** **** **** 1234");

        Page<AdminCardResponse> result = cardService.getAllCards(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(CardStatus.ACTIVE, result.getContent().get(0).getStatus());
    }

    @Test
    void getAllCards_withStatusFilter_success() {
        User user = User.builder().id(1L).name("Тест").surname("Тестов").build();
        Card card = Card.builder()
                .id(1L)
                .owner(user)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2027, 12, 31))
                .cardNumber("encryptedNumber")
                .build();

        when(cardRepository.findByStatus(eq(CardStatus.BLOCKED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardEncryptionUtil.decrypt("encryptedNumber")).thenReturn("1234567890121234");
        when(cardEncryptionUtil.mask("1234567890121234")).thenReturn("**** **** **** 1234");

        Page<AdminCardResponse> result = cardService.getAllCards("BLOCKED", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(CardStatus.BLOCKED, result.getContent().get(0).getStatus());
    }

    @Test
    void getAllCards_invalidStatus_throwsException() {
        assertThrows(InvalidCardStatusException.class,
                () -> cardService.getAllCards("INVALID", PageRequest.of(0, 10)));
    }
}