package com.example.transproducer.service;

import com.example.transproducer.model.Transaction;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TransactionScheduler {

    private final TransactionGenerator generator = new TransactionGenerator();
    private final TransactionPublisher publisher;

    public TransactionScheduler(TransactionPublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 1000)
    public void publishTransaction() {
        Transaction transaction = generator.generate();
        publisher.publish(transaction);
    }
}