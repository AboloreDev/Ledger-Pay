package com.abolore.ledger_pay.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.auth.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}