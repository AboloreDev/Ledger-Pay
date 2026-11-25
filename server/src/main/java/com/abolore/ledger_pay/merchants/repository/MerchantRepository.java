package com.abolore.ledger_pay.merchants.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.merchants.entity.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByEmail(String email);

    Optional<Merchant> findByEmail(String email);

    Optional<Merchant> findById(UUID merchantId);
}