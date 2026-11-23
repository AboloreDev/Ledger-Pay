package com.abolore.ledger_pay.auth.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.auth.entity.Admin;
import com.abolore.ledger_pay.auth.repository.AdminRepository;
import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Admin admin = adminRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> DomainException.of(ErrorCode.EMAIL_NOT_FOUND, email));

        return User.builder()
                .username(admin.getEmail())
                .password(admin.getPasswordHash())
                .roles(admin.getRole()
                        .name())
                .disabled(!admin.isEnabled())
                .build();
    }

}
