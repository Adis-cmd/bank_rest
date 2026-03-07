package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для создания карты")
public class CardCreateDto {

    @Schema(description = "ID пользователя", example = "1")
    @NotNull(message = "ID пользователя не должен быть пустым")
    private Long userId;

    @Schema(description = "Срок действия карты", example = "2027-12-31")
    @NotNull(message = "Срок действия не должен быть пустым")
    @Future(message = "Срок действия должен быть в будущем")
    private LocalDate expirationDate;
}
