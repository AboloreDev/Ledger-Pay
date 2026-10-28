
package com.abolore.ledger_pay.payment.controller;

import com.abolore.ledger_pay.payment.dto.PaymentCreationResult;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;
import com.abolore.ledger_pay.payment.rest.PaymentController;
import com.abolore.ledger_pay.payment.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PaymentService paymentService;

        @MockitoBean
        private PaymentCreationResult paymentCreationResult;

        @Test
        void shouldCreatePaymentAndReturn201() throws Exception {
                UUID id = UUID.randomUUID();

                Payment payment = new Payment.Builder()
                                .setReference("PAY-TEST-001")
                                .setAmount(new BigDecimal("5000.00"))
                                .setCurrency("NGN")
                                .build();

                ReflectionTestUtils.setField(payment, "id", id);

                PaymentCreationResult paymentCreationResult = new PaymentCreationResult(
                                payment,
                                "https://example.com/checkout");

                when(paymentService.createPayment(
                                "PAY-TEST-001",
                                new BigDecimal("5000.00"),
                                "NGN", "customer@example.com")).thenReturn(paymentCreationResult);

                mockMvc.perform(post("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "reference": "PAY-TEST-001",
                                                  "amount": 5000.00,
                                                  "currency": "NGN",
                                                  "customerEmail": "customer@example.com"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.reference").value("PAY-TEST-001"))
                                .andExpect(jsonPath("$.amount").value(5000.00))
                                .andExpect(jsonPath("$.currency").value("NGN"))
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.checkoutUrl")
                                                .value("https://example.com/checkout"));
        }

        @Test
        void shouldRejectInvalidPaymentRequest() throws Exception {
                mockMvc.perform(post("/api/v1/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "reference": "",
                                                  "amount": -100,
                                                  "currency": "ngn"
                                                }
                                                """))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnPaymentById() throws Exception {
                UUID id = UUID.randomUUID();

                Payment payment = new Payment.Builder()
                                .setReference("PAY-TEST-002")
                                .setAmount(new BigDecimal("2500.00"))
                                .setCurrency("NGN")
                                .build();

                ReflectionTestUtils.setField(payment, "id", id);

                when(paymentService.getPaymentById(id)).thenReturn(payment);

                mockMvc.perform(get("/api/v1/payments/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.reference").value("PAY-TEST-002"))
                                .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        void shouldReturn404WhenPaymentDoesNotExist() throws Exception {
                UUID id = UUID.randomUUID();

                when(paymentService.getPaymentById(id))
                                .thenThrow(new PaymentNotFoundExceptions(
                                                "Payment not found: " + id));

                mockMvc.perform(get("/api/v1/payments/{id}", id))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message")
                                                .value("Payment not found: " + id));
        }
}
