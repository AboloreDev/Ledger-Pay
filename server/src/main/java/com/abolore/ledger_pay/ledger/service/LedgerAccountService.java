package com.abolore.ledger_pay.ledger.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.ledger.entity.LedgerAccount;
import com.abolore.ledger_pay.ledger.entity.LedgerAccountStatus;
import com.abolore.ledger_pay.ledger.entity.LedgerAccountType;
import com.abolore.ledger_pay.ledger.repository.LedgerAccountRepository;

@Service
public class LedgerAccountService {

    private final LedgerAccountRepository ledgerAccountRepository;

    public LedgerAccountService(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public void initializeStandardAccounts() {
        System.out.println("Initializing standard ledger accounts...");
        createAccountIfMissing("PROVIDER_CLEARING", "Provider Clearing", LedgerAccountType.ASSET);

        createAccountIfMissing("MERCHANT_PAYABLE", "Merchant Payable", LedgerAccountType.LIABILITY);

        createAccountIfMissing("PLATFORM_FEE_REVENUE", "Platform Fee Revenue", LedgerAccountType.REVENUE);

        createAccountIfMissing("BANK_CASH", "Bank Cash", LedgerAccountType.ASSET);
    }

    public List<LedgerAccount> getAllAccounts() {
        return ledgerAccountRepository.findAll();
    }

    private void createAccountIfMissing(String code, String name, LedgerAccountType type) {

        if (ledgerAccountRepository.findByAccountCode(code).isPresent()) {
            return;
        }

        LedgerAccount account = new LedgerAccount.Builder()
                .setAccountCode(code)
                .setName(name)
                .setAccountType(type)
                .setCurrency("NGN")
                .setStatus(LedgerAccountStatus.ACTIVE)
                .build();

        ledgerAccountRepository.save(account);
    }
}
