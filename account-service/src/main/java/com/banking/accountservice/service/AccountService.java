package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class AccountService {
    private final AccountRepository accountRepository;
    private final SecureRandom random = new SecureRandom();

    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for : {}", request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Account with email " + request.getEmail() + " already exists");
        }

        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyWithdrawalLimit(
            request.getAccountType() == AccountType.SAVINGS
            ? new BigDecimal("100000")
            : new BigDecimal("500000")
        );

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully for : {}", savedAccount.getAccountNumber());

        return mapToAccountResponse(savedAccount);
    }

    public AccountResponse getAccount(String accountNumber) {
        log.info("Fetching account details for : {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));
        return mapToAccountResponse(account);
    }

    public BigDecimal getBalance(String accountNumber) {
        log.info("Fetching balance for account : {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));
        return account.getBalance();
    }

    public void blockAccount(String accountNumber) {
        log.info("Blocking account : {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));
        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Account blocked successfully : {}", accountNumber);
    }

    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting balance for account : {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));

        if(account.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new RuntimeException("Account is blocked: " + accountNumber);
        }

        if(account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account: " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Balance deducted successfully for account : {}", accountNumber);
    }

    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting balance for account : {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));

        if(account.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new RuntimeException("Account is blocked: " + accountNumber);
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Balance credited successfully for account : {}", accountNumber);
        log.info("New balance for account {}: {}", accountNumber, account.getBalance());
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = random.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhoneNumber(account.getPhoneNumber());
        response.setAccountType(account.getAccountType());
        response.setAccountStatus(account.getAccountStatus());
        response.setBalance(account.getBalance());
        response.setDailyWithdrawalLimit(account.getDailyWithdrawalLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}
