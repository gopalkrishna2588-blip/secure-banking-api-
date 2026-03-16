package com.banking.controller;

import com.banking.dto.ApiResponse;
import com.banking.dto.TransactionResponse;
import com.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{accountId}")
    @Operation(summary = "Get transaction history with pagination")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Page<TransactionResponse> history = transactionService.getHistory(
                accountId, from, to,
                PageRequest.of(page, Math.min(size, 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(ApiResponse.ok("History retrieved", history));
    }
}