package com.abolore.ledger_pay.merchant_keys.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKey;
import com.abolore.ledger_pay.merchant_keys.repository.MerchantApiKeyRepository;
import com.abolore.ledger_pay.merchants.entity.Merchant;

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

        String keyHash = hashKey(keyPrefix);

        MerchantApiKey merchantApiKey = MerchantApiKey.create(merchant, keyPrefix, keyHash);

        merchantApiKeyRepository.save(merchantApiKey);

        return rawKey;

    }

    public String hashKey(String rawKey) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA256");

            byte[] hash = msgDigest.digest(rawKey.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw DomainException.of(ErrorCode.INCORRECT_HASHING_ALGORITHM,
                    "SHA-256 algorithm unavailable" + exception);
        }
    }
}
