package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
}
