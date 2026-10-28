package com.abolore.ledger_pay.payment.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.payment.dto.CreatePaymentRequest;
import com.abolore.ledger_pay.payment.dto.PaymentCreationResult;
import com.abolore.ledger_pay.payment.dto.PaymentResponse;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentCreationResult payment = paymentService.createPayment(
                request.reference(),
                request.amount(),
                request.currency(),
                request.customerEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(
                        payment.payment(),
                        payment.checkoutUrl()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID id) {

        Payment payment = paymentService.getPaymentById(id);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(
            @PathVariable String reference) {

        Payment payment = paymentService.getPaymentByReference(reference);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/{id}/succeed")
    public ResponseEntity<PaymentResponse> markPaymentSucceeded(
            @PathVariable UUID id) {

        Payment payment = paymentService.markPaymentSucceeded(id);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
