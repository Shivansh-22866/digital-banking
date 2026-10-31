package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class AccountEventConsumer {

    private final AccountService accountService;

    @KafkaListener (topics = "transaction.completed")
    public void consumeTransactionEvent(
        @Payload Map<String, Object> payload
    ) {
        log.info("Consuming transaction event: {}", payload);

        try {
            String receiverAccountNumber = (String) payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal((String) payload.get("amount"));

            accountService.creditBalance(receiverAccountNumber, amount);
        }

        catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage());
        }
    }
    
    @KafkaListener (topics = "fraud.detected")
    public void consumeFraudDetected(
        @Payload Map<String, Object> payload
    ) {
        log.info("Consuming fraud detected event: {}", payload);

        try {
            String accountNumber = (String) payload.get("accountNumber");
            accountService.blockAccount(accountNumber);        }

        catch (Exception e) {
            log.error("Error processing fraud detected event: {}", e.getMessage());
        }
    }
}
