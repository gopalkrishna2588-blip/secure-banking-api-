package com.banking.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_account",     columnList = "account_id"),
        @Index(name = "idx_tx_idempotency", columnList = "idempotencyKey", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(unique = true)
    private String idempotencyKey;

    private String relatedAccountNumber;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAW, TRANSFER_DEBIT, TRANSFER_CREDIT
    }

    public enum TransactionStatus {
        SUCCESS, FAILED
    }
}