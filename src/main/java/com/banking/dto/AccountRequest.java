package com.banking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequest {

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100)
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.00", message = "Cannot be negative")
    private BigDecimal initialDeposit;
}