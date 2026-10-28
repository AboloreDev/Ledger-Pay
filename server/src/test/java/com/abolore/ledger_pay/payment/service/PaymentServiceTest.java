package com.abolore.ledger_pay.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.entity.JournalLineRequest;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.service.JournalPostingService;
import com.abolore.ledger_pay.outbox.OutboxEventFactory;
import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.PaymentInitiationPayload;
import com.abolore.ledger_pay.outbox.repository.OutboxEventRepository;
import com.abolore.ledger_pay.payment.dto.PaymentCreationResult;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.payment.exceptions.DuplicatePaymentException;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;
import com.abolore.ledger_pay.payment.provider.PaymentProvider;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private JournalPostingService journalPostingService;

        private PaymentService paymentService;

        @Mock
        private PaymentProvider paymentProvider;
        @Mock
        private OutboxEventRepository outboxEventRepository;
        @Mock
        private OutboxEventFactory outboxEventFactory;

        private Payment payment;
        private UUID paymentId;
        private UUID providerClearingAccountId;
        private UUID merchantPayableAccountId;

        @BeforeEach
        void setUp() {
                paymentId = UUID.randomUUID();
                providerClearingAccountId = UUID.randomUUID();
                merchantPayableAccountId = UUID.randomUUID();

                payment = new Payment.Builder()
                                .setReference("PAY-TEST-001")
                                .setAmount(new BigDecimal("5000.00"))
                                .setCurrency("NGN")
                                .build();

                ReflectionTestUtils.setField(payment, "id", paymentId);

                paymentService = new PaymentService(
                                paymentRepository,
                                journalPostingService,
                                providerClearingAccountId,
                                merchantPayableAccountId,
                                paymentProvider,
                                outboxEventRepository,
                                outboxEventFactory);
        }

        @Test
        void shouldCreatePaymentWithPendingStatus() {
                String customerEmail = "customer@example.com";

                when(paymentRepository.existsByReference("PAY-TEST-001"))
                                .thenReturn(false);

                when(paymentRepository.save(any(Payment.class)))
                                .thenAnswer(invocation -> {
                                        Payment savedPayment = invocation.getArgument(0);

                                        if (savedPayment.getId() == null) {
                                                ReflectionTestUtils.setField(
                                                                savedPayment,
                                                                "id",
                                                                UUID.randomUUID());
                                        }

                                        return savedPayment;
                                });

                OutboxEvent outboxEvent = mock(OutboxEvent.class);

                when(outboxEventFactory.paymentInitiationRequested(
                                any(PaymentInitiationPayload.class)))
                                .thenReturn(outboxEvent);

                PaymentCreationResult result = paymentService.createPayment(
                                "PAY-TEST-001",
                                new BigDecimal("5000.00"),
                                "NGN",
                                customerEmail);

                assertNotNull(result);
                assertNotNull(result.payment());

                assertEquals(PaymentStatus.PENDING, result.payment().getStatus());
                assertEquals("PAY-TEST-001", result.payment().getReference());

                assertEquals(
                                0,
                                new BigDecimal("5000.00").compareTo(result.payment().getAmount()));

                // Checkout URL is unavailable until the worker contacts the provider.
                assertEquals(null, result.checkoutUrl());

                verify(paymentRepository).existsByReference("PAY-TEST-001");
                verify(paymentRepository, times(1)).save(any(Payment.class));

                ArgumentCaptor<PaymentInitiationPayload> payloadCaptor = ArgumentCaptor
                                .forClass(PaymentInitiationPayload.class);

                verify(outboxEventFactory).paymentInitiationRequested(
                                payloadCaptor.capture());

                PaymentInitiationPayload payload = payloadCaptor.getValue();

                assertEquals(result.payment().getId(), payload.paymentId());
                assertEquals("PAY-TEST-001", payload.reference());
                assertEquals(0, new BigDecimal("5000.00").compareTo(payload.amount()));
                assertEquals("NGN", payload.currency());
                assertEquals(customerEmail, payload.customerEmail());

                verify(outboxEventRepository).save(outboxEvent);

                verifyNoInteractions(journalPostingService, paymentProvider);
        }

        @Test
        void shouldRejectDuplicatePaymentReference() {
                when(paymentRepository.existsByReference("PAY-TEST-001"))
                                .thenReturn(true);

                assertThrows(
                                DuplicatePaymentException.class,
                                () -> paymentService.createPayment(
                                                "PAY-TEST-001",
                                                new BigDecimal("5000.00"),
                                                "NGN", "customer@example.com"));

                verify(paymentRepository, never()).save(any(Payment.class));
                verifyNoInteractions(journalPostingService);
        }

        @Test
        void shouldRejectPaymentThatDoesNotExist() {
                when(paymentRepository.findById(paymentId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                PaymentNotFoundExceptions.class,
                                () -> paymentService.markPaymentSucceeded(paymentId));

                verifyNoInteractions(journalPostingService);
        }

        @Test
        void shouldMarkPaymentFailedWithoutPostingJournal() {
                when(paymentRepository.findById(paymentId))
                                .thenReturn(Optional.of(payment));

                when(paymentRepository.save(any(Payment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                Payment result = paymentService.markPaymentFailed(paymentId);

                assertEquals(PaymentStatus.FAILED, result.getStatus());

                verify(paymentRepository).save(payment);
                verifyNoInteractions(journalPostingService);
        }

        @Test
        void shouldMarkPaymentSucceededAndPostBalancedJournalEntry() {

                when(paymentRepository.findById(paymentId))
                                .thenReturn(Optional.of(payment));

                when(paymentRepository.save(any(Payment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Payment result = paymentService.markPaymentSucceeded(paymentId);

                // Assert payment state
                assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());

                // Capture the journal request sent to the journal service
                ArgumentCaptor<JournalEntryRequest> captor = ArgumentCaptor.forClass(JournalEntryRequest.class);

                verify(journalPostingService).postJournalEntry(captor.capture());

                JournalEntryRequest request = captor.getValue();

                assertEquals("PAYMENT-" + payment.getReference(), request.reference());
                assertEquals("PAYMENT", request.sourceType());
                assertEquals(paymentId, request.sourceId());
                assertEquals(paymentId, request.sourceEventId());

                List<JournalLineRequest> lines = request.lines();

                assertEquals(2, lines.size());

                JournalLineRequest debit = lines.get(0);
                JournalLineRequest credit = lines.get(1);

                assertEquals(JournalSides.DEBIT, debit.side());
                assertEquals(JournalSides.CREDIT, credit.side());

                assertEquals(0, new BigDecimal("5000.00").compareTo(debit.amount()));
                assertEquals(0, new BigDecimal("5000.00").compareTo(credit.amount()));

                assertEquals("NGN", debit.currency());
                assertEquals("NGN", credit.currency());

                assertEquals(providerClearingAccountId, debit.ledgerAccountId());
                assertEquals(merchantPayableAccountId, credit.ledgerAccountId());

                verify(paymentRepository).save(payment);
        }

        @Test
        void shouldNotCompletePaymentWhenJournalPostingFails() {
                // Arrange
                when(paymentRepository.findById(paymentId))
                                .thenReturn(Optional.of(payment));

                doThrow(new RuntimeException("Journal posting failed"))
                                .when(journalPostingService)
                                .postJournalEntry(any(JournalEntryRequest.class));

                // Act & Assert
                assertThrows(
                                RuntimeException.class,
                                () -> paymentService.markPaymentSucceeded(paymentId));

                // The journal failure should prevent the service from saving
                // the payment as successful.
                verify(paymentRepository, never()).save(any(Payment.class));

                // The mocked entity was mutated in memory, so restore its state
                // before reusing it in another test.
                assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        }

}