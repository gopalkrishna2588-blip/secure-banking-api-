package com.banking.dto;

import com.banking.model.Transaction;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String relatedAccountNumber;
    private String description;
    private String status;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType().name())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .relatedAccountNumber(t.getRelatedAccountNumber())
                .description(t.getDescription())
                .status(t.getStatus().name())
                .idempotencyKey(t.getIdempotencyKey())
                .createdAt(t.getCreatedAt())
                .build();
    }
}