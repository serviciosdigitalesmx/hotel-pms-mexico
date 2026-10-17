package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Deposit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped deposit persistence with reference idempotency. */
public interface DepositRepository extends JpaRepository<Deposit, UUID> {
  /** Lists deposits for one tenant's invoice in receipt order.
   *
   * @param hotelId tenant identifier
   * @param invoiceId invoice identifier
   * @return invoice deposits ordered by receipt time
   */
  List<Deposit> findAllByHotelIdAndInvoiceIdOrderByReceivedAtAsc(UUID hotelId, UUID invoiceId);

  /** Lists deposits in selected lifecycle states for an invoice.
   *
   * @param hotelId tenant identifier
   * @param invoiceId invoice identifier
   * @param statuses included deposit states
   * @return deposits matching the requested states
   */
  List<Deposit> findAllByHotelIdAndInvoiceIdAndStatusIn(UUID hotelId, UUID invoiceId,
      java.util.Collection<Deposit.Status> statuses);

  /** Finds a deposit by its tenant-scoped idempotency key.
   *
   * @param hotelId tenant identifier
   * @param referenceKey idempotency key
   * @return matching deposit, if present
   */
  java.util.Optional<Deposit> findByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);
}
