package com.abolore.ledger_pay.merchant_keys.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKey;
import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKeyStatus;

public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {

    Optional<MerchantApiKey> findByKeyPrefixAndStatus(
            String keyPrefix,
            MerchantApiKeyStatus status);

    List<MerchantApiKey> findAllByMerchant_Id(UUID merchantId);

}