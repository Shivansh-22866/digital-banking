package com.banking.transactionservice.service;

import org.springframework.stereotype.Service;

import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";

    public TransactionResponse transfer(TransactionRequest transferRequest) {
        log.info("SAGA Start - Transfer: {} -> {} amount: {}",
            transferRequest.getSenderAccountNumber(),
            transferRequest.getReceiverAccountNumber(),
            transferRequest.getAmount()
        );

        
    }
}
