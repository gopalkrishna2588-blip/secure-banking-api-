package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  AccountService     accountService;
    @MockBean  TransactionService transactionService;

    private AccountResponse sampleAccount() {
        return AccountResponse.builder()
                .id(1L).accountNumber("ACC-20240315-000001")
                .ownerName("Alice").email("alice@test.com")
                .balance(new BigDecimal("1000.00")).status("ACTIVE")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/accounts: valid → 201")
    void createAccount_201() throws Exception {
        AccountRequest req = new AccountRequest();
        req.setOwnerName("Alice");
        req.setEmail("alice@test.com");
        req.setInitialDeposit(new BigDecimal("1000.00"));

        when(accountService.createAccount(any())).thenReturn(sampleAccount());

        mockMvc.perform(post("/api/accounts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC-20240315-000001"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/accounts: missing name → 400")
    void createAccount_missingName_400() throws Exception {
        AccountRequest req = new AccountRequest();
        req.setEmail("alice@test.com");
        req.setInitialDeposit(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/accounts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ownerName").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/accounts: invalid email → 400")
    void createAccount_invalidEmail_400() throws Exception {
        AccountRequest req = new AccountRequest();
        req.setOwnerName("Alice");
        req.setEmail("not-valid");
        req.setInitialDeposit(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/accounts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/accounts/{id}: found → 200")
    void getAccount_200() throws Exception {
        when(accountService.getAccount(1L)).thenReturn(sampleAccount());
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ownerName").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/accounts/{id}: no token → 401")
    void getAccount_noToken_401() throws Exception {
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/accounts/{id}/deposit: negative amount → 400")
    void deposit_negativeAmount_400() throws Exception {
        AmountRequest req = new AmountRequest();
        req.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/accounts/1/deposit").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}