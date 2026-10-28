package com.banking.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;


public class AccountResponse {
    private String id;

    private String accountNumber;

    private String accountHolderName;

    private String email;

    private String phoneNumber;

    private AccountType accountType;

    private AccountStatus accountStatus;

    private BigDecimal balance;

    private BigDecimal dailyWithdrawalLimit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
