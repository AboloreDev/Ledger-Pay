package com.abolore.ledger_pay.merchants.mapper;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.dto.MerchantProfileResponse;

@Service
public class MerchantProfileMapper {

    public MerchantProfileResponse mapResponse(MerchantPrincipal principal) {
        return new MerchantProfileResponse(
                principal.merchantId(),
                principal.email(),
                principal.name(),
                principal.status(),
                principal.createdAt());
    }
}
