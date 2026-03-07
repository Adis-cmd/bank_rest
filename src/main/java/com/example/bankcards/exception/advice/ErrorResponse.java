package com.example.bankcards.exception.advice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponse {

    @Schema(description = "HTTP статус ошибки", example = "404")
    private int status;

    @Schema(description = "Сообщение об ошибке", example = "Пользователь не найден")
    private String message;

    @Schema(description = "Время возникновения ошибки", example = "2025-10-03T15:20:30")
    private LocalDateTime timestamp;
}