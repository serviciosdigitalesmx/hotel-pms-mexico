package com.hotelpms.guest.repository;

import com.hotelpms.guest.model.Device;
import com.hotelpms.guest.dto.response.DeviceCatalogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Tenant-scoped persistence operations for devices. */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    /** Groups active customer-owned devices into the tenant's observed model catalog. */
    @Query("select new com.hotelpms.guest.dto.response.DeviceCatalogEntry("
        + "min(d.category), min(coalesce(d.manufacturer, '')), min(coalesce(d.model, '')), count(d.id)) "
        + "from Device d where d.hotelId = :hotelId "
        + "group by lower(trim(d.category)), lower(coalesce(trim(d.manufacturer), '')), "
        + "lower(coalesce(trim(d.model), '')) "
        + "order by count(d.id) desc")
    List<DeviceCatalogEntry> catalogByHotelId(@Param("hotelId") UUID hotelId);

    /**
     * Finds a device by tenant, customer and identifier.
     *
     * @param id device identifier.
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @return matching device.
     */
    Optional<Device> findByIdAndHotelIdAndGuestId(UUID id, UUID hotelId, UUID guestId);

    /**
     * Lists devices for a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @return matching devices.
     */
    List<Device> findAllByHotelIdAndGuestId(UUID hotelId, UUID guestId);

    /** Searches device identity fields within the active tenant. */
    @Query("select d from Device d where d.hotelId = :hotelId and "
        + "(lower(d.category) like lower(concat('%', :query, '%')) "
        + "or lower(coalesce(d.manufacturer, '')) like lower(concat('%', :query, '%')) "
        + "or lower(coalesce(d.model, '')) like lower(concat('%', :query, '%')) "
        + "or lower(coalesce(d.serialNumber, '')) like lower(concat('%', :query, '%')) "
        + "or lower(coalesce(d.imei, '')) like lower(concat('%', :query, '%'))) "
        + "order by d.updatedAt desc")
    Page<Device> searchTenantDevices(@Param("hotelId") UUID hotelId,
        @Param("query") String query, Pageable pageable);

    /**
     * Finds a device by serial number within a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @param serialNumber serial number.
     * @return matching device.
     */
    Optional<Device> findByHotelIdAndGuestIdAndSerialNumber(UUID hotelId, UUID guestId, String serialNumber);

    /**
     * Finds a device by IMEI within a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @param imei IMEI value.
     * @return matching device.
     */
    Optional<Device> findByHotelIdAndGuestIdAndImei(UUID hotelId, UUID guestId, String imei);

    boolean existsByHotelIdAndGuestIdAndSerialNumberIgnoreCase(UUID hotelId, UUID guestId, String serialNumber);

    boolean existsByHotelIdAndGuestIdAndSerialNumberIgnoreCaseAndIdNot(
        UUID hotelId, UUID guestId, String serialNumber, UUID id);

    boolean existsByHotelIdAndImeiIgnoreCase(UUID hotelId, String imei);

    boolean existsByHotelIdAndImeiIgnoreCaseAndIdNot(UUID hotelId, String imei, UUID id);
}
