package com.banking.transactionservice.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.events.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";

    public TransactionResponse transfer(TransactionRequest transferRequest) {
        log.info("SAGA Start - Transfer: {} -> {} amount: {}",
            transferRequest.getSenderAccountNumber(),
            transferRequest.getReceiverAccountNumber(),
            transferRequest.getAmount()
        );

        accountServiceClient.deductBalance(transferRequest.getSenderAccountNumber(), transferRequest.getAmount());

        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(transferRequest.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(transferRequest.getReceiverAccountNumber());
        transaction.setAmount(transferRequest.getAmount());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transaction.setDescription(transferRequest.getDescription());
        transaction.setReferenceNumber(UUID.randomUUID().toString());
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as PROCESSING : {}", savedTransaction.getId());
        

        TransactionInitiatedEvent transactionInitiatedEvent = new TransactionInitiatedEvent(
            savedTransaction.getId(),
            savedTransaction.getSenderAccountNumber(),
            savedTransaction.getReceiverAccountNumber(),
            savedTransaction.getAmount(),
            savedTransaction.getDescription()
        );

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, savedTransaction.getId(), transactionInitiatedEvent);
        log.info("SAGA Step 2 - TransactionInitiatedEvent sent to Kafka for transactionId: {}", savedTransaction.getId());

        return mapToTransactionResponse(savedTransaction);
    }

    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
        return mapToTransactionResponse(transaction);
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        return transactionRepository.findBySenderAccountNumberOrderByCreatedAtDesc(accountNumber)
            .stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getSenderAccountNumber(),
            transaction.getReceiverAccountNumber(),
            transaction.getAmount(),
            transaction.getTransactionType(),
            transaction.getTransactionStatus(),
            transaction.getDescription(),
            transaction.getFailureReason(),
            transaction.getReferenceNumber(),
            transaction.getCreatedAt(),
            transaction.getCompletedAt()
        );
    }
}
