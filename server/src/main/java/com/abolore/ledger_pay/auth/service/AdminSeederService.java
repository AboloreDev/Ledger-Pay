package com.abolore.ledger_pay.auth.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.auth.entity.Admin;
import com.abolore.ledger_pay.auth.entity.AdminRole;
import com.abolore.ledger_pay.auth.repository.AdminRepository;
import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;

@Service
public class AdminSeederService {

    private static final Logger logger = LoggerFactory.getLogger(Admin.class);

    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final String email;
    private final String password;

    public AdminSeederService(PasswordEncoder passwordEncoder, AdminRepository adminRepository,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.email = email;
        this.password = password;
    }

    public void runAdminSeeder() {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw DomainException.of(ErrorCode.ADMIN_BAD_REQUEST,
                    "Admin email and password must be provided in application properties.");
        }

        if (email.isBlank() || password.isBlank()) {
            throw DomainException.of(ErrorCode.ADMIN_BAD_REQUEST,
                    "Both ADMIN_EMAIL and ADMIN_PASSWORD must be configured");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        String passwordHash = passwordEncoder.encode(password);

        if (!adminRepository.existsByEmail(email)) {
            Admin admin = new Admin.Builder()
                    .setEmail(normalizedEmail).setPasswordHash(passwordHash)
                    .setRole(AdminRole.ADMIN).setEnabled(true)
                    .build();

            adminRepository.save(admin);

            logger.info("Admin user created with email: " + email);
        } else {
            logger.info("Admin user already exists with email: " + email);
        }
    }
}
