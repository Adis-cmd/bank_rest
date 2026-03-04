package com.example.bankcards.entity;

import com.example.bankcards.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_from_card", columnList = "from_card_id"),
        @Index(name = "idx_transactions_to_card", columnList = "to_card_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.SUCCESS;
}
