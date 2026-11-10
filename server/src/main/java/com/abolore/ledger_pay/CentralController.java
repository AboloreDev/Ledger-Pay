package com.abolore.ledger_pay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This is checking for the app health
@RestController
@RequestMapping("/api/v1/ledger-pay")
public class CentralController {
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Server is running");
    }
}
