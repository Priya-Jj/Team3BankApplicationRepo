package com.example.bankapi.service;

import com.example.bankapi.model.TransactionStat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransactionStatsPublisher {
    private final KafkaTemplate<String, TransactionStat> kafkaTemplate;
    private final String topic;

    public TransactionStatsPublisher(KafkaTemplate<String, TransactionStat> kafkaTemplate,
                                     @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(String type, BigDecimal amount) {
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        TransactionStat stat = new TransactionStat(type, scaled);
        try {
            kafkaTemplate.send(topic, type, stat).whenComplete((res, ex) -> {
                if (ex != null) {
                    System.err.println("Failed to send transaction stat: " + ex.getMessage());
                } else {
                    System.out.println("Sent transaction stat: key=" + type + " value=" + stat);
                }
            });
        } catch (Exception e) {
            System.err.println("Kafka send failed: " + e.getMessage());
        }
    }
}
