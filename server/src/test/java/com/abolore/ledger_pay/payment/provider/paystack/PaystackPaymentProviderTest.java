
package com.abolore.ledger_pay.payment.provider.paystack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.abolore.ledger_pay.payment.provider.PaymentInitiationRequest;
import com.abolore.ledger_pay.payment.provider.PaymentInitiationResult;
import com.abolore.ledger_pay.payment.provider.PaymentVerificationResult;

class PaystackPaymentProviderTest {

    private MockRestServiceServer server;
    private PaystackPaymentProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        provider = new PaystackPaymentProvider(
                builder,
                "https://api.paystack.co",
                "test-secret");
    }

    @Test
    void initiatePaymentShouldReturnAuthorizationUrlAndReference() {
        server.expect(requestTo(
                "https://api.paystack.co/transaction/initialize"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email")
                        .value("customer@example.com"))
                .andExpect(jsonPath("$.amount").value(2500000))
                .andExpect(jsonPath("$.currency").value("NGN"))
                .andExpect(jsonPath("$.reference").value("payment-ref-001"))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "message": "Authorization URL created",
                          "data": {
                            "authorization_url": "https://checkout.example/test",
                            "reference": "payment-ref-001"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentInitiationRequest request = new PaymentInitiationRequest(
                UUID.randomUUID(),
                "payment-ref-001",
                new BigDecimal("25000.00"),
                "NGN",
                "customer@example.com");

        PaymentInitiationResult result = provider.initiatePayment(request);

        assertEquals("payment-ref-001", result.providerReference());
        assertEquals(
                "https://checkout.example/test",
                result.checkoutUrl());

        server.verify();
    }

    @Test
    void verifyPaymentShouldReturnProviderTransactionDetails() {
        server.expect(requestTo(
                "https://api.paystack.co/transaction/verify/payment-ref-001"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "message": "Verification successful",
                          "data": {
                            "status": "success",
                            "reference": "payment-ref-001",
                            "amount": 2500000,
                            "currency": "NGN"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentVerificationResult result = provider.verifyPayment("payment-ref-001");

        assertEquals("payment-ref-001", result.providerReference());
        assertEquals("success", result.status());
        assertEquals(2500000L, result.amountInMinorUnits());
        assertEquals("NGN", result.currency());

        server.verify();
    }
}
