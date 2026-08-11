package com.example.transproducer.service;

import com.example.transproducer.model.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransactionPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionPublisher.class);
    private static final String TOPIC = "transactions";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Transaction transaction) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, transaction.accountId(), transaction);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction {} for account {}",
                        transaction.id(), transaction.accountId(), ex);
            } else {
                var metadata = result.getRecordMetadata();
                log.info("Sent {} {} {} {} -> partition {}, offset {}",
                        transaction.id(),
                        transaction.accountId(),
                        transaction.type(),
                        transaction.amount(),
                        metadata.partition(),
                        metadata.offset());
            }
        });
    }
}