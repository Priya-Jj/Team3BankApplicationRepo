package com.example.transproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransproducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransproducerApplication.class, args);
	}
}