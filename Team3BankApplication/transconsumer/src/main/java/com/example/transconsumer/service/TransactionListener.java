package com.example.transconsumer.service;

import com.example.transconsumer.model.TransactionStatistic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);

    private final StatisticsCollector collector;

    public TransactionListener(StatisticsCollector collector) {
        this.collector = collector;
    }

    @KafkaListener(topics = "transaction-stats")
    public void handle(ConsumerRecord<String, TransactionStatistic> record) {
        TransactionStatistic statistic = record.value();

        log.info("Received key={} partition={} offset={}",
                record.key(), record.partition(), record.offset());

        collector.collect(statistic);
    }
}