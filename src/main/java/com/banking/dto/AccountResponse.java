package com.banking.dto;

import com.banking.model.Account;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String ownerName;
    private String email;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse from(Account a) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .ownerName(a.getOwnerName())
                .email(a.getEmail())
                .balance(a.getBalance())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}