package com.example.bankcards.dto;

import com.example.bankcards.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO карты для отображения пользователю")
public class CardDto {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Срок действия", example = "2027-12-31")
    private LocalDate expirationDate;
}
