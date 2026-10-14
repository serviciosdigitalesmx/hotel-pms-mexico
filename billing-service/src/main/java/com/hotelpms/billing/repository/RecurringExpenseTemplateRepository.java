package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.RecurringExpenseTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Tenant-scoped recurring expense templates. */
public interface RecurringExpenseTemplateRepository extends JpaRepository<RecurringExpenseTemplate, UUID> {
  List<RecurringExpenseTemplate> findAllByHotelIdOrderByNextDueAtAsc(UUID hotelId);

  Optional<RecurringExpenseTemplate> findByIdAndHotelId(UUID id, UUID hotelId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select template from RecurringExpenseTemplate template where template.id = :id and template.hotelId = :hotelId")
  Optional<RecurringExpenseTemplate> findForUpdateByIdAndHotelId(
      @Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
