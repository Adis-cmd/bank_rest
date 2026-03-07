package com.example.bankcards.exception.advice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Ответ об ошибке валидации")
public class ValidationErrorResponse {

    @Schema(description = "HTTP статус ошибки", example = "400")
    private int status;

    @Schema(description = "Ошибки по каждому полю", example = "{\"email\": \"Неверный формат email\", \"password\": \"Пароль обязателен\"}")
    private Map<String, String> errors;

    @Schema(description = "Время возникновения ошибки", example = "2025-10-03T15:20:30")
    private LocalDateTime timestamp;
}
