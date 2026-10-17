package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped expense access. */
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  /** Finds a generated expense for one recurring template period.
   *
   * @param hotelId tenant identifier
   * @param recurringTemplateId recurring template identifier
   * @param recurringPeriodDueAt scheduled period
   * @return matching expense, if generated
   */
  Optional<Expense> findByHotelIdAndRecurringTemplateIdAndRecurringPeriodDueAt(
      UUID hotelId, UUID recurringTemplateId, LocalDate recurringPeriodDueAt);

  /**
   * Lists expenses for a tenant.
   *
   * @param hotelId tenant identifier
   * @return expenses ordered by creation time
   */
  List<Expense> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);

  /** Lists expenses by business date while including unpaid obligations due in the requested period.
   *
   * @param hotelId tenant identifier
   * @param fromDate first report date
   * @param toDate last report date
   * @return expenses whose business or due date falls in the period
   */
  @Query(value = "select * from expenses where hotel_id = :hotelId and "
      + "((coalesce(spent_at, cast(created_at as date)) between :fromDate and :toDate) "
      + "or (paid = false and due_at between :fromDate and :toDate)) "
      + "order by created_at desc", nativeQuery = true)
  List<Expense> findForReport(@Param("hotelId") UUID hotelId,
      @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

  /** Finds an expense only within the owning tenant.
   *
   * @param id expense identifier
   * @param hotelId tenant identifier
   * @return tenant-owned expense, if present
   */
  Optional<Expense> findByIdAndHotelId(UUID id, UUID hotelId);

  /** Locks an expense for a tenant-scoped update.
   *
   * @param id expense identifier
   * @param hotelId tenant identifier
   * @return locked tenant-owned expense, if present
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Expense e where e.id = :id and e.hotelId = :hotelId")
  Optional<Expense> findForUpdateByIdAndHotelId(@Param("id") UUID id,
      @Param("hotelId") UUID hotelId);
}
