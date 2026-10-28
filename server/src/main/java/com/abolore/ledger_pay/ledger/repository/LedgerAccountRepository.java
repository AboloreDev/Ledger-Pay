package com.abolore.ledger_pay.ledger.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.ledger.entity.LedgerAccount;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByAccountCode(String accountCode);
}
