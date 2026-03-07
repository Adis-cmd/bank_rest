package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для перевода между картами")
public class TransferDto {

    @Schema(description = "ID карты списания", example = "1")
    @NotNull(message = "ID карты списания не должен быть пустым")
    private Long fromCard;

    @Schema(description = "ID карты получателя", example = "2")
    @NotNull(message = "ID карты получателя не должен быть пустым")
    private Long toCard;

    @Schema(description = "Сумма перевода", example = "500.00")
    @NotNull(message = "Сумма не должна быть пустой")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}