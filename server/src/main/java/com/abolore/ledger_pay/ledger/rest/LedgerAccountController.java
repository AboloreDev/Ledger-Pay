package com.abolore.ledger_pay.ledger.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.ledger.entity.LedgerAccount;
import com.abolore.ledger_pay.ledger.service.LedgerAccountService;

@RestController
@RequestMapping("/api/v1/ledger-accounts")
public class LedgerAccountController {

    private final LedgerAccountService ledgerAccountService;

    public LedgerAccountController(LedgerAccountService ledgerAccountService) {
        this.ledgerAccountService = ledgerAccountService;
    }

    @GetMapping("/")
    public List<LedgerAccount> getAll() {
        return ledgerAccountService.getAllAccounts();
    }

}
