package com.abolore.ledger_pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LedgerPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LedgerPayApplication.class, args);
	}

}
