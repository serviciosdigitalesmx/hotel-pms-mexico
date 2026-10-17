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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

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

    @Query("select count(d.id) from Device d where d.hotelId = :hotelId and d.active = true and "
        + "lower(trim(d.category)) = lower(trim(:category)) and "
        + "lower(coalesce(trim(d.manufacturer), '')) = lower(trim(:manufacturer)) and "
        + "lower(coalesce(trim(d.model), '')) = lower(trim(:model))")
    long countCatalogIdentity(@Param("hotelId") UUID hotelId, @Param("category") String category,
        @Param("manufacturer") String manufacturer, @Param("model") String model);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Device d set d.category = :targetCategory, d.manufacturer = :targetManufacturer, "
        + "d.model = :targetModel, d.updatedAt = :updatedAt where d.hotelId = :hotelId and d.active = true and "
        + "lower(trim(d.category)) = lower(trim(:sourceCategory)) and "
        + "lower(coalesce(trim(d.manufacturer), '')) = lower(trim(:sourceManufacturer)) and "
        + "lower(coalesce(trim(d.model), '')) = lower(trim(:sourceModel))")
    int normalizeCatalogIdentity(@Param("hotelId") UUID hotelId,
        @Param("sourceCategory") String sourceCategory,
        @Param("sourceManufacturer") String sourceManufacturer,
        @Param("sourceModel") String sourceModel,
        @Param("targetCategory") String targetCategory,
        @Param("targetManufacturer") String targetManufacturer,
        @Param("targetModel") String targetModel,
        @Param("updatedAt") LocalDateTime updatedAt);

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
