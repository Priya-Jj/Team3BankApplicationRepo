package com.example.bankapi.service;

import com.example.bankapi.model.TransactionStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionStatsPublisher.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionStatsPublisher")
class TransactionStatsPublisherTest {

    @Mock
    private KafkaTemplate<String, TransactionStat> kafkaTemplate;

    private TransactionStatsPublisher publisher;

    private final String topic = "test-topic";

    @BeforeEach
    void setUp() {
        // Construct publisher with mocked KafkaTemplate and a literal topic
        publisher = new TransactionStatsPublisher(kafkaTemplate, topic);
    }

    @Test
    @DisplayName("publish scales amount to 2 decimal places and sends correct key/value")
    void publishScalesAmountAndSends() {
        BigDecimal input = new BigDecimal("1.235"); // will round HALF_UP -> 1.24
        BigDecimal expected = input.setScale(2, RoundingMode.HALF_UP);

        // Stub send to return a successfully completed future
        when(kafkaTemplate.send(eq(topic), eq("TYPE_A"), any(TransactionStat.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish("TYPE_A", input);

        ArgumentCaptor<TransactionStat> captor = ArgumentCaptor.forClass(TransactionStat.class);
        verify(kafkaTemplate, times(1)).send(eq(topic), eq("TYPE_A"), captor.capture());

        TransactionStat sent = captor.getValue();
        assertThat(sent).isNotNull();
        assertThat(sent.type()).isEqualTo("TYPE_A");
        assertThat(sent.amount()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("publish tolerates KafkaTemplate.send throwing an exception")
    void publishHandlesSendThrowing() {
        BigDecimal input = new BigDecimal("2.5");
        // Make kafkaTemplate.send throw immediately
        when(kafkaTemplate.send(anyString(), anyString(), any(TransactionStat.class)))
                .thenThrow(new RuntimeException("send failed"));

        // Should not propagate the exception
        assertDoesNotThrow(() -> publisher.publish("TYPE_B", input));

        // send should have been called once
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(TransactionStat.class));
    }

    @Test
    @DisplayName("publish tolerates the returned future completing exceptionally")
    void publishHandlesFutureCompletingExceptionally() {
        BigDecimal input = new BigDecimal("3.1415");

        // Return a future that completes exceptionally
        CompletableFuture<Object> badFuture = new CompletableFuture<>();
        badFuture.completeExceptionally(new RuntimeException("async failure"));

        when(kafkaTemplate.send(anyString(), anyString(), any(TransactionStat.class)))
                .thenReturn((CompletableFuture) badFuture);

        // Should not throw even though the future completes exceptionally
        assertDoesNotThrow(() -> publisher.publish("TYPE_C", input));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(TransactionStat.class));
    }
}