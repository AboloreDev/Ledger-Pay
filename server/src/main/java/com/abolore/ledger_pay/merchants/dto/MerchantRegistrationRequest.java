package com.abolore.ledger_pay.merchants.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantRegistrationRequest(
        @NotBlank @Size(max = 150) String name,

        @NotBlank @Email @Size(max = 255) String email) {
}
