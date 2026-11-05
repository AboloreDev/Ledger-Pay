package com.abolore.ledger_pay.payment.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReference(String reference);

    Optional<Payment> findById(UUID paymentId);

    boolean existsByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") UUID id);

    // Payments whose deadline has passed and are awaiting reconciliation.
    @Query("""
                SELECT p FROM Payment p
                WHERE p.status = :status
                AND p.expiresAt <= :now
                AND p.expiresAt > :graceCutoff
            """)
    List<Payment> findPaymentsForReconciliation(@Param("status") PaymentStatus status, @Param("now") Instant now,
            @Param("graceCutoff") Instant graceCutoff);

    // Payments whose deadline and grace period have both passed.
    @Query("""
                SELECT p FROM Payment p
                WHERE p.status = :status
                AND p.expiresAt <= :graceCutoff
            """)
    List<Payment> findPaymentsReadyForExpiry(@Param("status") PaymentStatus status,
            @Param("graceCutoff") Instant graceCutoff);
}
