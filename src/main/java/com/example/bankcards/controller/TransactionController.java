package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Transaction Controller", description = "API для управления транзакциями и операциями с картами")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final CardService cardService;

    /**
     * Выполняет перевод между картами текущего пользователя.
     *
     * @param principal данные текущего пользователя
     * @param dto       данные перевода (ID карт и сумма)
     * @return 200 если перевод выполнен успешно
     */
    @PostMapping
    @Operation(summary = "Перевод между картами", description = "Выполняет перевод средств между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные или недостаточно средств"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<Void> transfer(
            Principal principal,
            @Valid @RequestBody TransferDto dto) {
        cardService.transferCard(principal.getName(), dto);
        return ResponseEntity.ok().build();
    }



}
