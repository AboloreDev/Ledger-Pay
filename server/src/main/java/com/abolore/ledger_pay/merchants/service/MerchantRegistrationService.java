package com.abolore.ledger_pay.merchants.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationRequest;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationResponse;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.repository.MerchantRepository;

@Service
public class MerchantRegistrationService {

    private final MerchantApiKeyService merchantApiKeyService;
    private final MerchantRepository merchantRepository;

    public MerchantRegistrationService(MerchantApiKeyService merchantApiKeyService,
            MerchantRepository merchantRepository) {
        this.merchantApiKeyService = merchantApiKeyService;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public MerchantRegistrationResponse register(MerchantRegistrationRequest registrationRequest) {
        String email = registrationRequest.email().trim().toLowerCase(Locale.ROOT);

        if (merchantRepository.existsByEmail(email)) {
            throw DomainException.of(ErrorCode.MERCHANT_EXISTS, "A merchant with this email already exists");
        }

        String name = registrationRequest.name().trim();

        Merchant merchant = Merchant.create(name, email);
        merchantRepository.save(merchant);

        String apiKey = merchantApiKeyService.generateKey(merchant);

        return new MerchantRegistrationResponse(
                merchant.getId(),
                merchant.getName(),
                merchant.getEmail(),
                merchant.getStatus().name(),
                apiKey,
                merchant.getCreatedAt());
    }

    public String generateNewApiKey(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> DomainException.of(ErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        String newApiKey = merchantApiKeyService.generateKey(merchant);

        return newApiKey;
    }
}
