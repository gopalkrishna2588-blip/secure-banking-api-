package com.banking.service;

import com.banking.audit.Auditable;
import com.banking.dto.AccountRequest;
import com.banking.dto.AccountResponse;
import com.banking.exception.CustomException;
import com.banking.model.Account;
import com.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AtomicLong counter = new AtomicLong(0);

    @Auditable(action = "CREATE_ACCOUNT", entityType = "Account")
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new CustomException.DuplicateAccountException(
                    "Email already registered: " + request.getEmail());
        }

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .balance(request.getInitialDeposit())
                .status(Account.AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        return AccountResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public AccountResponse getBalance(Long id) {
        return AccountResponse.from(findById(id));
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new CustomException.AccountNotFoundException(
                        "Account not found: " + id));
    }

    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomException.AccountNotFoundException(
                        "Account not found: " + accountNumber));
    }

    public void assertActive(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new CustomException.AccountSuspendedException(
                    "Account " + account.getAccountNumber() + " is not active");
        }
    }

    private String generateAccountNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("ACC-%s-%06d", date, counter.incrementAndGet());
    }
}