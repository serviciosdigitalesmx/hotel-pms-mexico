package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository for Payment entities. */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  /**
   * Finds all payments associated with a specific invoice.
   *
   *
   * @param invoiceId the invoice UUID
   * @return a list of payments
   */
  List<Payment> findByInvoiceId(UUID invoiceId);

  /**
   * Finds a payment by invoice and transaction reference.
   *
   * @param invoiceId invoice identifier.
   *
   * @param transactionReference external transaction reference.
   * @return matching payment, if any.
   */
  Optional<Payment> findByInvoiceIdAndTransactionReference(
      UUID invoiceId, String transactionReference);
}
