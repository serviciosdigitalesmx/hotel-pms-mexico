package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped expense access. */
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  Optional<Expense> findByHotelIdAndRecurringTemplateIdAndRecurringPeriodDueAt(
      UUID hotelId, UUID recurringTemplateId, LocalDate recurringPeriodDueAt);
  /**
   * Lists expenses for a tenant.
   *
   * @param hotelId tenant identifier.
   * @return expenses ordered by creation time.
   */
  List<Expense> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);

  Optional<Expense> findByIdAndHotelId(UUID id, UUID hotelId);
}
