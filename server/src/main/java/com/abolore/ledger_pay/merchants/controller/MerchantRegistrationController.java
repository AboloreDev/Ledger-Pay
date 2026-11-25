package com.abolore.ledger_pay.merchants.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.dto.MerchantProfileResponse;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationRequest;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationResponse;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.mapper.MerchantProfileMapper;
import com.abolore.ledger_pay.merchants.service.MerchantRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantRegistrationController {

    private final MerchantRegistrationService registrationService;
    private final MerchantProfileMapper merchantProfileMapper;

    public MerchantRegistrationController(
            MerchantRegistrationService registrationService, MerchantProfileMapper merchantProfileMapper) {
        this.registrationService = registrationService;
        this.merchantProfileMapper = merchantProfileMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<MerchantRegistrationResponse> register(
            @Valid @RequestBody MerchantRegistrationRequest request) {

        MerchantRegistrationResponse response = registrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/generate-key")
    public ResponseEntity<String> generateNewApiKey(@RequestBody UUID merchantId) {

        String response = registrationService.generateNewApiKey(merchantId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MerchantProfileResponse> getProfile(Authentication authentication) {

        MerchantPrincipal merchant = (MerchantPrincipal) authentication.getPrincipal();

        MerchantProfileResponse response = merchantProfileMapper.mapResponse(merchant);

        return ResponseEntity.ok(response);
    }
}