package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts")
public class AccountController {

    private final AccountService     accountService;
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", accountService.createAccount(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account details")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Account retrieved", accountService.getAccount(id)));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "View account balance")
    public ResponseEntity<ApiResponse<AccountResponse>> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Balance retrieved", accountService.getBalance(id)));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Deposit successful",
                transactionService.deposit(id, request)));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal successful",
                transactionService.withdraw(id, request)));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Transfer successful",
                transactionService.transfer(request)));
    }
}