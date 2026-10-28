package com.abolore.ledger_pay.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreatePaymentRequest(
        @NotBlank(message = "Payment reference is required") String reference,

        @NotNull(message = "Amount is required") @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero") @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places") BigDecimal amount,

        @NotBlank(message = "Currency is required") @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase code") String currency,

        @NotBlank(message = "Customer email is required") @Email(message = "Customer email must be valid") String customerEmail) {
}