package com.banking.service;

import com.banking.audit.Auditable;
import com.banking.dto.AmountRequest;
import com.banking.dto.TransactionResponse;
import com.banking.dto.TransferRequest;
import com.banking.exception.CustomException;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository     accountRepository;
    private final AccountService        accountService;

    // ── DEPOSIT ───────────────────────────────────────────────────────────────
    @Auditable(action = "DEPOSIT", entityType = "Transaction")
    @Transactional
    public TransactionResponse deposit(Long accountId, AmountRequest req) {

        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new CustomException.AccountNotFoundException(
                        "Account not found: " + accountId));

        accountService.assertActive(account);

        BigDecimal newBalance = account.getBalance().add(req.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = saveTx(account, Transaction.TransactionType.DEPOSIT,
                req.getAmount(), newBalance, null, req.getDescription(), null);

        log.info("Deposited {} to {}. Balance: {}", req.getAmount(), account.getAccountNumber(), newBalance);
        return TransactionResponse.from(tx);
    }

    // ── WITHDRAW ──────────────────────────────────────────────────────────────
    @Auditable(action = "WITHDRAW", entityType = "Transaction")
    @Transactional
    public TransactionResponse withdraw(Long accountId, AmountRequest req) {

        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new CustomException.AccountNotFoundException(
                        "Account not found: " + accountId));

        accountService.assertActive(account);

        if (account.getBalance().compareTo(req.getAmount()) < 0) {
            throw new CustomException.InsufficientFundsException(
                    "Insufficient funds. Available: " + account.getBalance()
                            + ", Requested: " + req.getAmount());
        }

        BigDecimal newBalance = account.getBalance().subtract(req.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = saveTx(account, Transaction.TransactionType.WITHDRAW,
                req.getAmount(), newBalance, null, req.getDescription(), null);

        log.info("Withdrew {} from {}. Balance: {}", req.getAmount(), account.getAccountNumber(), newBalance);
        return TransactionResponse.from(tx);
    }

    // ── TRANSFER ──────────────────────────────────────────────────────────────
    @Auditable(action = "TRANSFER", entityType = "Transaction")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse transfer(TransferRequest req) {

        // 1. Idempotency check
        if (transactionRepository.existsByIdempotencyKey(req.getIdempotencyKey())) {
            Transaction existing = transactionRepository
                    .findByIdempotencyKey(req.getIdempotencyKey()).orElseThrow();
            throw new CustomException.DuplicateTransactionException(
                    "Transfer already processed", TransactionResponse.from(existing));
        }

        // 2. Validate not same account
        if (req.getFromAccountNumber().equals(req.getToAccountNumber())) {
            throw new CustomException.InvalidTransactionException(
                    "Source and destination accounts must be different");
        }

        // 3. Fetch accounts
        Account from = accountService.findByAccountNumber(req.getFromAccountNumber());
        Account to   = accountService.findByAccountNumber(req.getToAccountNumber());

        accountService.assertActive(from);
        accountService.assertActive(to);

        // 4. Lock in ascending ID order — prevents deadlocks
        Account first  = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to   : from;

        first  = accountRepository.findByIdWithLock(first.getId()).orElseThrow();
        second = accountRepository.findByIdWithLock(second.getId()).orElseThrow();

        if (from.getId() < to.getId()) { from = first; to = second; }
        else                            { to = first;  from = second; }

        // 5. Balance check
        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new CustomException.InsufficientFundsException(
                    "Insufficient funds. Available: " + from.getBalance());
        }

        // 6. Update balances
        BigDecimal fromNew = from.getBalance().subtract(req.getAmount());
        BigDecimal toNew   = to.getBalance().add(req.getAmount());
        from.setBalance(fromNew);
        to.setBalance(toNew);
        accountRepository.save(from);
        accountRepository.save(to);

        // 7. Record both ledger entries
        Transaction debit = saveTx(from, Transaction.TransactionType.TRANSFER_DEBIT,
                req.getAmount(), fromNew, to.getAccountNumber(),
                req.getDescription(), req.getIdempotencyKey());

        saveTx(to, Transaction.TransactionType.TRANSFER_CREDIT,
                req.getAmount(), toNew, from.getAccountNumber(),
                req.getDescription(), req.getIdempotencyKey() + "_CREDIT");

        log.info("Transfer {} from {} to {}", req.getAmount(),
                from.getAccountNumber(), to.getAccountNumber());
        return TransactionResponse.from(debit);
    }

    // ── TRANSACTION HISTORY ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(Long accountId,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                Pageable pageable) {
        accountService.findById(accountId);

        Page<Transaction> page = (from != null && to != null)
                ? transactionRepository.findByAccountIdAndDateRange(accountId, from, to, pageable)
                : transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);

        return page.map(TransactionResponse::from);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Transaction saveTx(Account account, Transaction.TransactionType type,
                               BigDecimal amount, BigDecimal balanceAfter,
                               String relatedAccount, String description,
                               String idempotencyKey) {
        return transactionRepository.save(Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .relatedAccountNumber(relatedAccount)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build());
    }
}