package com.abolore.ledger_pay.merchant_keys.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKey;
import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKeyStatus;
import com.abolore.ledger_pay.merchant_keys.repository.MerchantApiKeyRepository;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

@Service
public class MerchantApiKeyService {

    public static final String KEY_PREFIX = "lp_test_";
    public static final int KEY_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final MerchantApiKeyRepository merchantApiKeyRepository;

    public MerchantApiKeyService(MerchantApiKeyRepository merchantApiKeyRepository) {
        this.merchantApiKeyRepository = merchantApiKeyRepository;
    }

    public String generateKey(Merchant merchant) {
        byte[] randomBytes = new byte[KEY_LENGTH];

        secureRandom.nextBytes(randomBytes);

        String randomValue = Base64.getUrlEncoder()
                .withoutPadding().encodeToString(randomBytes);

        String rawKey = KEY_PREFIX + randomValue;

        String keyPrefix = rawKey.substring(0, 16);

        String keyHash = hashKey(rawKey);

        MerchantApiKey merchantApiKey = MerchantApiKey.create(merchant, keyPrefix, keyHash);

        merchantApiKeyRepository.save(merchantApiKey);

        return rawKey;

    }

    public String hashKey(String rawKey) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");

            byte[] hash = msgDigest.digest(rawKey.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw DomainException.of(ErrorCode.INCORRECT_HASHING_ALGORITHM,
                    "SHA-256 algorithm unavailable" + exception);
        }
    }

    @Transactional(readOnly = true)
    public MerchantPrincipal authenticate(String rawKey) {
        if (rawKey == null ||
                !rawKey.startsWith("lp_test_") ||
                rawKey.length() <= 16) {
            throw DomainException.of(ErrorCode.MERCHANT_INVALID_API_KEY, "Invalid Api Key");
        }

        String keyPrefix = rawKey.substring(0, 16);

        String hashedPrefix = hashKey(rawKey);

        MerchantApiKey merchantApiKey = merchantApiKeyRepository
                .findByKeyPrefixAndStatus(keyPrefix, MerchantApiKeyStatus.ACTIVE)
                .orElseThrow(() -> DomainException.of(ErrorCode.MERCHANT_INVALID_API_KEY, "Invalid Api Key"));

        boolean isValid = MessageDigest.isEqual(
                merchantApiKey.getKeyHash().getBytes(StandardCharsets.UTF_8),
                hashedPrefix.getBytes(StandardCharsets.UTF_8));

        if (!isValid) {
            throw DomainException.of(ErrorCode.MERCHANT_INVALID_API_KEY, "Invalid Api Key");
        }

        Merchant merchant = merchantApiKey.getMerchant();

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw DomainException.of(ErrorCode.INVALID_MERCHANT_STATUS, "Merchant isn't an active merchant");
        }

        return new MerchantPrincipal(
                merchant.getId(), merchant.getEmail(), merchant.getName(), merchant.getStatus(),
                merchant.getCreatedAt());
    }
}
