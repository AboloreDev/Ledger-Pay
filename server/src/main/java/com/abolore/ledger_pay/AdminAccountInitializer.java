package com.abolore.ledger_pay;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.abolore.ledger_pay.auth.service.AdminSeederService;

@Configuration
public class AdminAccountInitializer {

    @Bean
    CommandLineRunner initializeAdminAccount(AdminSeederService adminSeederService) {

        return args -> adminSeederService.runAdminSeeder();
    }
}
