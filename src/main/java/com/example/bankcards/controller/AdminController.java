package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Controller", description = "Административные операции (управление пользователями, картами и транзакциями)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CardService cardService;

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
