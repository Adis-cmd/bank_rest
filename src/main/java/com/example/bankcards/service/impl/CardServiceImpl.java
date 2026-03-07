package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardStatusException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardEncryptionUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final TransactionService transactionService;

    @Override
    public void requestBlockCard(Long cardId, String email) {
        User user = userService.findUserByEmail(email);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found!"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Карту можно заблокировать только если она активна");
        }

        card.setStatus(CardStatus.BLOCK_REQUESTED);
        cardRepository.save(card);
    }

    @Override
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found!"));

        if (card.getStatus() != CardStatus.BLOCK_REQUESTED) {
            throw new CardNotActiveException("Можно заблокировать только карту которые подали запрос на блокировку");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }


    @Override
    public void activeCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found!"));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardNotActiveException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }


    @Override
    public BigDecimal getCardById(Long cardId, String email) {
        User user = userService.findUserByEmail(email);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found!!"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }
        return card.getBalance();
    }

    @Transactional
    @Override
    public void transferCard(String email, TransferDto dto) {
        User user = userService.findUserByEmail(email);

        Card fromCard = cardRepository.findById(dto.getFromCard())
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + dto.getFromCard()));

        Card toCard = cardRepository.findById(dto.getToCard())
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + dto.getToCard()));

        validateTransfer(user, fromCard, toCard, dto.getAmount());

        fromCard.setBalance(fromCard.getBalance().subtract(dto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(dto.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        transactionService.transfer(fromCard, toCard, dto.getAmount());
    }

    private void validateTransfer(User user, Card fromCard, Card toCard, BigDecimal amount) {
        if (!fromCard.getOwner().getId().equals(user.getId()) ||
                !toCard.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }
        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalArgumentException("Нельзя переводить на ту же карту");
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Карта списания заблокирована или истекла");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Карта получателя заблокирована или истекла");
        }
        if (fromCard.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Срок действия карты списания истёк");
        }
        if (toCard.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Срок действия карты получателя истёк");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте");
        }
    }

    @Override
    public void createCard(CardCreateDto dto) {
        User user = userService.findById(dto.getUserId());

        String rawCardNumber = generateUniqueCardNumber();
        String encryptedCardNumber = cardEncryptionUtil.encrypt(rawCardNumber);

        Card card = Card.builder()
                .owner(user)
                .cardNumber(encryptedCardNumber)
                .expirationDate(dto.getExpirationDate())
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();
        cardRepository.save(card);
    }

    @Override
    public Page<CardDto> cardsUser(String status, String email, Pageable pageable) {
        User user = userService.findUserByEmail(email);


        Page<Card> cards;
        if (status != null && !status.isBlank()) {
            CardStatus cardStatus;
            try {
                cardStatus = CardStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidCardStatusException(status);
            }
            cards = cardRepository.findByOwnerIdAndStatus(user.getId(), cardStatus, pageable);
        } else {
            cards = cardRepository.findAllByOwnerId(user.getId(), pageable);
        }


        return cards.map(card -> CardDto.builder()
                .id(card.getId())
                .maskedCardNumber(cardEncryptionUtil.mask(cardEncryptionUtil.decrypt(card.getCardNumber())))
                .status(card.getStatus())
                .balance(card.getBalance())
                .expirationDate(card.getExpirationDate())
                .build());
    }

    public String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardEncryptionUtil.encrypt(cardNumber)));
        return cardNumber;
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }


    @Override
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardNotActiveException("Нельзя удалить активную карту." +
                    " Сначала заблокируйте её.");
        }

        transactionService.deleteTransaction(card.getId(),card.getId());
        cardRepository.delete(card);
    }
}
