package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Controller", description = "Административные операции (управление пользователями, картами и транзакциями)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    /**
     * Возвращает список всех карт в системе с пагинацией и фильтрацией по статусу.
     *
     * @param status статус карты для фильтрации (опционально)
     * @param page   номер страницы (по умолчанию 0)
     * @param size   размер страницы (по умолчанию 10)
     * @return страница с картами всех пользователей
     */
    @GetMapping("cards")
    public ResponseEntity<PageResponse<AdminCardResponse>> cards(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCardResponse> cards = cardService.getAllCards(status, pageable);

        PageResponse<AdminCardResponse> response = PageResponse.<AdminCardResponse>builder()
                .content(cards.getContent())
                .totalElements(cards.getTotalElements())
                .totalPages(cards.getTotalPages())
                .pageNumber(cards.getNumber())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает список всех пользователей с пагинацией.
     *
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return страница с пользователями
     */
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserDto>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserDto> users = userService.getAllUser(pageable);

        PageResponse<UserDto> response = PageResponse.<UserDto>builder()
                .content(users.getContent())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .pageNumber(users.getNumber())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя
     * @return 200 если пользователь удалён
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("Пользователь успешно удалён");
    }

    /**
     * Возвращает список карт конкретного пользователя с пагинацией.
     *
     * @param userId ID пользователя
     * @param page   номер страницы (по умолчанию 0)
     * @param size   размер страницы (по умолчанию 10)
     * @return страница с картами пользователя
     */
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<PageResponse<AdminCardResponse>> cardUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCardResponse> cards = cardService.getCardsByUserId(userId, pageable);

        PageResponse<AdminCardResponse> response = PageResponse.<AdminCardResponse>builder()
                .content(cards.getContent())
                .totalElements(cards.getTotalElements())
                .totalPages(cards.getTotalPages())
                .pageNumber(cards.getNumber())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Создаёт новую карту для указанного пользователя.
     *
     * @param dto данные для создания карты
     * @return 201 если карта создана
     */
    @PostMapping("/cards")
    @Operation(summary = "Создание карты", description = "Создаёт новую карту для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    public ResponseEntity<Void> createCard(@Valid @RequestBody CardCreateDto dto) {
        cardService.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Блокирует карту пользователя.
     *
     * @param cardId ID карты
     * @return 200 если карта заблокирована
     */
    @PutMapping("/cards/{cardId}/block")
    @Operation(summary = "Блокировка карты", description = "Блокирует карту пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<String> blockCard(@PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Карта успешно заблокирована");
    }

    /**
     * Активирует карту пользователя.
     *
     * @param cardId ID карты
     * @return 200 если карта активирована
     */
    @PutMapping("/cards/{cardId}/activate")
    @Operation(summary = "Активация карты", description = "Активирует карту пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<String> activateCard(@PathVariable Long cardId) {
        cardService.activeCard(cardId);
        return ResponseEntity.ok("Карта успешно активирована");
    }

    /**
     * Удаляет карту по ID (только для администратора).
     *
     * @param cardId ID карты
     * @return 200 если карта удалена
     */
    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Удаление карты", description = "Удаляет карту по ID (только для администратора)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить активную карту")
    })
    public ResponseEntity<String> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта успешно удалена");
    }
}
