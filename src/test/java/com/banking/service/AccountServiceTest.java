package com.banking.service;

import com.banking.dto.AccountRequest;
import com.banking.dto.AccountResponse;
import com.banking.exception.CustomException;
import com.banking.model.Account;
import com.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock  AccountRepository accountRepository;
    @InjectMocks AccountService accountService;

    private AccountRequest request;
    private Account account;

    @BeforeEach
    void setUp() {
        request = new AccountRequest();
        request.setOwnerName("Alice");
        request.setEmail("alice@test.com");
        request.setInitialDeposit(new BigDecimal("1000.00"));

        account = Account.builder()
                .id(1L).accountNumber("ACC-20240315-000001")
                .ownerName("Alice").email("alice@test.com")
                .balance(new BigDecimal("1000.00"))
                .status(Account.AccountStatus.ACTIVE).version(0L).build();
    }

    @Test
    @DisplayName("createAccount: success")
    void createAccount_success() {
        when(accountRepository.existsByEmail(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);

        AccountResponse result = accountService.createAccount(request);

        assertThat(result.getOwnerName()).isEqualTo("Alice");
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("createAccount: duplicate email throws exception")
    void createAccount_duplicateEmail() {
        when(accountRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(CustomException.DuplicateAccountException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAccount: found")
    void getAccount_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThat(accountService.getAccount(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAccount: not found throws exception")
    void getAccount_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(CustomException.AccountNotFoundException.class);
    }

    @Test
    @DisplayName("assertActive: ACTIVE passes")
    void assertActive_active() {
        assertThatCode(() -> accountService.assertActive(account)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertActive: SUSPENDED throws")
    void assertActive_suspended() {
        account.setStatus(Account.AccountStatus.SUSPENDED);
        assertThatThrownBy(() -> accountService.assertActive(account))
                .isInstanceOf(CustomException.AccountSuspendedException.class);
    }
}