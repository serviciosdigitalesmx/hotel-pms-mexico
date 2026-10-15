package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Deposit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped deposit persistence with reference idempotency. */
public interface DepositRepository extends JpaRepository<Deposit, UUID> {
  List<Deposit> findAllByHotelIdAndInvoiceIdOrderByReceivedAtAsc(UUID hotelId, UUID invoiceId);
  List<Deposit> findAllByHotelIdAndInvoiceIdAndStatusIn(UUID hotelId, UUID invoiceId,
      java.util.Collection<Deposit.Status> statuses);
  java.util.Optional<Deposit> findByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);
}
