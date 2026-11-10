package com.abolore.ledger_pay;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.abolore.ledger_pay.ledger.service.LedgerAccountService;

@Configuration
public class LedgerAccountInitializer {

    @Bean
    CommandLineRunner initializeLedger(LedgerAccountService ledgerAccountService) {

        return args -> ledgerAccountService.initializeStandardAccounts();
    }
}
