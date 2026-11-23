package com.abolore.ledger_pay.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.auth.dto.LoginRequest;
import com.abolore.ledger_pay.auth.dto.LoginResponse;
import com.abolore.ledger_pay.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        if (response != null && response.accessToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
