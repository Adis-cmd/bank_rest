package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Tag(name = "Cards Controller", description = "Общие операции с картами (доступны пользователям и администраторам)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    /**
     * Возвращает карты текущего пользователя с пагинацией и фильтрацией по статусу.
     *
     * @param principal данные текущего авторизованного пользователя
     * @param status    статус карты для фильтрации (опционально)
     * @param page      номер страницы (по умолчанию 0)
     * @param size      размер страницы (по умолчанию 10)
     * @return страница с картами пользователя
     */
    @GetMapping
    @Operation(summary = "Получение карт пользователя", description = "Возвращает карты текущего пользователя с пагинацией и фильтрацией по статусу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен"),
            @ApiResponse(responseCode = "400", description = "Неверный статус карты")
    })
    public ResponseEntity<PageResponse<CardDto>> cards(
            Principal principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CardDto> cards = cardService.cardsUser(status, principal.getName(), pageable);

        PageResponse<CardDto> response = PageResponse.<CardDto>builder()
                .content(cards.getContent())
                .totalElements(cards.getTotalElements())
                .totalPages(cards.getTotalPages())
                .pageNumber(cards.getNumber())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает баланс карты текущего пользователя.
     *
     * @param cardId    ID карты
     * @param principal данные текущего пользователя
     * @return баланс карты
     */
    @GetMapping("{cardId}/balance")
    @Operation(summary = "Получение баланса карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс получен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю")
    })
    public ResponseEntity<BigDecimal> balance(@PathVariable Long cardId, Principal principal) {
        BigDecimal balance = cardService.getCardById(cardId, principal.getName());
        return ResponseEntity.ok(balance);
    }

    /**
     * Запрашивает блокировку карты текущим пользователем.
     *
     * @param cardId    ID карты
     * @param principal данные текущего пользователя
     * @return 200 если запрос отправлен успешно
     */
    @PutMapping("{cardId}/block-request")
    @Operation(summary = "Запрос блокировки карты", description = "Пользователь запрашивает блокировку своей карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос на блокировку отправлен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю")
    })
    public ResponseEntity<String> blockRequestCard(@PathVariable Long cardId, Principal principal) {
        cardService.requestBlockCard(cardId, principal.getName());
        return ResponseEntity.ok("Запрос на блокировку карты отправлен");
    }

}
