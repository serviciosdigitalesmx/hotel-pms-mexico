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
  /** Lists a tenant's recurring expense templates by next due date.
   *
   * @param hotelId tenant identifier
   * @return templates ordered by next due date
   */
  List<RecurringExpenseTemplate> findAllByHotelIdOrderByNextDueAtAsc(UUID hotelId);

  /** Finds a recurring template only within the owning tenant.
   *
   * @param id template identifier
   * @param hotelId tenant identifier
   * @return tenant-owned template, if present
   */
  Optional<RecurringExpenseTemplate> findByIdAndHotelId(UUID id, UUID hotelId);

  /** Locks a tenant-owned template while generating its scheduled expense.
   *
   * @param id template identifier
   * @param hotelId tenant identifier
   * @return locked tenant-owned template, if present
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select template from RecurringExpenseTemplate template where template.id = :id and template.hotelId = :hotelId")
  Optional<RecurringExpenseTemplate> findForUpdateByIdAndHotelId(
      @Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
