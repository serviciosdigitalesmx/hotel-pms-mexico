package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped repair order persistence, locks, and operational reporting. */
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {
    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param hotelId parameter.
     * @return operation result.
     */
    Optional<ServiceOrder> findByIdAndHotelId(UUID id, UUID hotelId);

    /**
     * Locks a service order after verifying tenant ownership.
     *
     * @param id service order identifier
     * @param hotelId tenant identifier
     * @return locked order, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ServiceOrder o where o.id = :id and o.hotelId = :hotelId")
    Optional<ServiceOrder> findForUpdateByIdAndHotelId(
            @Param("id") UUID id, @Param("hotelId") UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @return operation result.
     */
    List<ServiceOrder> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);

    /**
     * Lists a tenant's repair orders in a requested page.
     *
     * @param hotelId tenant identifier
     * @param pageable pagination and sorting
     * @return page of tenant-owned orders
     */
    Page<ServiceOrder> findAllByHotelId(UUID hotelId, Pageable pageable);

    /**
     * Finds orders due for notification that have not been completed or cancelled.
     *
     * @param now current timestamp used for the overdue cutoff
     * @return a bounded list of overdue tenant orders eligible for notification
     */
    /**
     * Counts repair orders for each requested customer within a tenant.
     *
     * @param hotelId tenant identifier
     * @param customerIds customer identifiers
     * @return rows containing customer identifiers and order counts
     */
    @Query(
            value =
                    "select o.* from service_orders o where o.promised_at <= :now and o.status not"
                        + " in ('COMPLETED','CANCELLED') and not exists (select 1 from"
                        + " operational_notification_outbox n where n.hotel_id=o.hotel_id and"
                        + " n.event_type='DELIVERY_DUE' and n.source_key = ('DELIVERY_DUE:' ||"
                        + " o.id::text || ':' || o.promised_at::date::text)) and exists (select 1"
                        + " from operational_notification_preferences p where p.hotel_id=o.hotel_id"
                        + " and coalesce((p.preferences->'DELIVERY_DUE'->>'EMAIL')::boolean,true)"
                        + " and jsonb_array_length(coalesce(p.email_recipients->'DELIVERY_DUE','[]'::jsonb))"
                        + " > 0) order by o.promised_at asc limit 25",
            nativeQuery = true)
    List<ServiceOrder> findOverdueOrders(@Param("now") LocalDateTime now);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param customerId parameter.
     * @return operation result.
     */
    List<ServiceOrder> findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(
            UUID hotelId, UUID customerId);

    @Query(
            "select serviceOrder.customerId, count(serviceOrder.id) from ServiceOrder serviceOrder"
                + " where serviceOrder.hotelId = :hotelId and serviceOrder.customerId in"
                + " :customerIds group by serviceOrder.customerId")
    List<Object[]> countOrdersForCustomers(
            @Param("hotelId") UUID hotelId, @Param("customerIds") Collection<UUID> customerIds);
}
