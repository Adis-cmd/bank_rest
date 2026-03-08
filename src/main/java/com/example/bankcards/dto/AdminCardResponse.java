package com.example.bankcards.dto;

import com.example.bankcards.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ с данными карты для администратора")
public class AdminCardResponse {

    @Schema(description = "Имя владельца карты", example = "Ivan")
    private String name;

    @Schema(description = "Фамилия владельца карты", example = "Ivanov")
    private String surname;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Срок действия карты", example = "2027-12-31")
    private LocalDate expirationDate;
}
