package com.banking.frauddetectionservice.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class FraudDetectionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = "${transaction.initiated}", groupId = "${fraud-detection-group}")
    public void consumeTransactionInitiated(
        @Payload Map<String, Object> payload
    ) {
        log.info("Transaction recieved for fraud detection: {}", payload.get("transactionId"));

        try {
            fraudDetectionService.checkTransaction(payload);
        }

        catch (Exception e) {
            log.error("Error processing transaction: {}", e.getMessage());
        }
    }
}
