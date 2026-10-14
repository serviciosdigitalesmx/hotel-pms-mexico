package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.RecurringExpenseTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped recurring expense templates. */
public interface RecurringExpenseTemplateRepository extends JpaRepository<RecurringExpenseTemplate, UUID> {
  List<RecurringExpenseTemplate> findAllByHotelIdOrderByNextDueAtAsc(UUID hotelId);

  Optional<RecurringExpenseTemplate> findByIdAndHotelId(UUID id, UUID hotelId);
}
