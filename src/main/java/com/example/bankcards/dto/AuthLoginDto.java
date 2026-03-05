package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для входа в систему")
public class AuthLoginDto {

    @Schema(description = "Email пользователя", example = "petrov@gmail.com")
    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email не должен быть пустым")
    private String email;

    @Schema(description = "Пароль пользователя", example = "yourPassword")
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}