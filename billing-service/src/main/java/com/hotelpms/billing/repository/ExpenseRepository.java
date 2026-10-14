package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Expense;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Tenant-scoped expense access. */
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
  /**
   * Lists expenses for a tenant.
   *
   * @param hotelId tenant identifier.
   * @return expenses ordered by creation time.
   */
  List<Expense> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);

  Optional<Expense> findByIdAndHotelId(UUID id, UUID hotelId);
}
