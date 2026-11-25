package com.abolore.ledger_pay.payment.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReference(String reference);

    Optional<Payment> findById(UUID paymentId);

    boolean existsByReference(String reference);

    // This works for the internal reconciliation with the payment provider
    List<Payment> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant periodStart, Instant periodEnd);

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

    // For reconciling
    List<Payment> findAllByIdIn(Collection<UUID> paymentIds);

    // Scoping to payment and merchant id
    @Query("""
            SELECT p FROM Payment p
            WHERE p.id = :paymentId
            AND p.merchant.id = :merchantId
            """)
    Optional<Payment> findByIdAndMerchantId(@Param("paymentId") UUID paymentId,
            @Param("merchantId") UUID merchantId);

    // Retrieving merchant
    @Query("""
            SELECT p FROM Payment p
            WHERE p.merchant.id = :merchantId
            """)
    Optional<Merchant> findMerchantById(@Param("merchantId") UUID merchantId);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.reference = :reference
            AND p.merchant.id = :merchantId
            """)
    Optional<Payment> findByReferenceAndMerchant_Id(@Param("reference") String reference,
            @Param("merchantId") UUID merchantId);

    Page<Payment> findAllByMerchant_Id(UUID merchantId, Pageable pageable);

    Page<Payment> findAllByMerchant_Id(Pageable pageable);
}
