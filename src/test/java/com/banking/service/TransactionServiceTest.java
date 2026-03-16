package com.banking.service;

import com.banking.dto.AmountRequest;
import com.banking.dto.TransactionResponse;
import com.banking.dto.TransferRequest;
import com.banking.exception.CustomException;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository     accountRepository;
    @Mock AccountService        accountService;
    @InjectMocks TransactionService transactionService;

    private Account alice, bob;

    @BeforeEach
    void setUp() {
        alice = Account.builder().id(1L).accountNumber("ACC-001")
                .balance(new BigDecimal("2000.00"))
                .status(Account.AccountStatus.ACTIVE).version(0L).build();

        bob = Account.builder().id(2L).accountNumber("ACC-002")
                .balance(new BigDecimal("500.00"))
                .status(Account.AccountStatus.ACTIVE).version(0L).build();
    }

    @Test
    @DisplayName("deposit: balance increases")
    void deposit_success() {
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.save(any())).thenReturn(alice);
        when(transactionRepository.save(any())).thenReturn(
                tx(alice, Transaction.TransactionType.DEPOSIT, "300.00", "2300.00"));

        transactionService.deposit(1L, amountReq("300.00"));

        assertThat(alice.getBalance()).isEqualByComparingTo("2300.00");
    }

    @Test
    @DisplayName("withdraw: sufficient balance decreases")
    void withdraw_success() {
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.save(any())).thenReturn(alice);
        when(transactionRepository.save(any())).thenReturn(
                tx(alice, Transaction.TransactionType.WITHDRAW, "500.00", "1500.00"));

        transactionService.withdraw(1L, amountReq("500.00"));

        assertThat(alice.getBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("withdraw: insufficient funds throws")
    void withdraw_insufficientFunds() {
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(alice));
        assertThatThrownBy(() -> transactionService.withdraw(1L, amountReq("9999.00")))
                .isInstanceOf(CustomException.InsufficientFundsException.class);
    }

    @Test
    @DisplayName("transfer: both balances updated")
    void transfer_success() {
        String key = UUID.randomUUID().toString();
        when(transactionRepository.existsByIdempotencyKey(key)).thenReturn(false);
        when(accountService.findByAccountNumber("ACC-001")).thenReturn(alice);
        when(accountService.findByAccountNumber("ACC-002")).thenReturn(bob);
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(bob));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenReturn(
                tx(alice, Transaction.TransactionType.TRANSFER_DEBIT, "200.00", "1800.00"));

        transactionService.transfer(transferReq("ACC-001", "ACC-002", "200.00", key));

        assertThat(alice.getBalance()).isEqualByComparingTo("1800.00");
        assertThat(bob.getBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("transfer: same account throws")
    void transfer_sameAccount() {
        String key = UUID.randomUUID().toString();
        when(transactionRepository.existsByIdempotencyKey(key)).thenReturn(false);
        assertThatThrownBy(() ->
                transactionService.transfer(transferReq("ACC-001", "ACC-001", "100.00", key)))
                .isInstanceOf(CustomException.InvalidTransactionException.class);
    }

    @Test
    @DisplayName("transfer: duplicate key returns existing")
    void transfer_duplicateKey() {
        when(transactionRepository.existsByIdempotencyKey("dup-key")).thenReturn(true);
        when(transactionRepository.findByIdempotencyKey("dup-key")).thenReturn(
                Optional.of(tx(alice, Transaction.TransactionType.TRANSFER_DEBIT, "100.00", "1900.00")));
        assertThatThrownBy(() ->
                transactionService.transfer(transferReq("ACC-001", "ACC-002", "100.00", "dup-key")))
                .isInstanceOf(CustomException.DuplicateTransactionException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AmountRequest amountReq(String amount) {
        AmountRequest r = new AmountRequest();
        r.setAmount(new BigDecimal(amount));
        return r;
    }

    private TransferRequest transferReq(String from, String to, String amount, String key) {
        TransferRequest r = new TransferRequest();
        r.setFromAccountNumber(from);
        r.setToAccountNumber(to);
        r.setAmount(new BigDecimal(amount));
        r.setIdempotencyKey(key);
        return r;
    }

    private Transaction tx(Account acc, Transaction.TransactionType type,
                           String amount, String balanceAfter) {
        return Transaction.builder().id(1L).account(acc).type(type)
                .amount(new BigDecimal(amount)).balanceAfter(new BigDecimal(balanceAfter))
                .status(Transaction.TransactionStatus.SUCCESS).build();
    }
}