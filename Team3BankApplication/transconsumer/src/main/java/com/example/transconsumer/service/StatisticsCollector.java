package com.example.transconsumer.service;

import com.example.transconsumer.model.TransactionStatistic;

public interface StatisticsCollector {
    void collect(TransactionStatistic statistic);
}