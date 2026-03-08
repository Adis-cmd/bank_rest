package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO пользователя")
public class UserDto {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "Ivan")
    private String name;

    @Schema(description = "Фамилия пользователя", example = "Ivanov")
    private String surname;
}
