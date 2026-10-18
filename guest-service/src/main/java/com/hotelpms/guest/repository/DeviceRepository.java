package com.hotelpms.guest.repository;

import com.hotelpms.guest.dto.response.DeviceCatalogEntry;
import com.hotelpms.guest.model.Device;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Tenant-scoped persistence operations for devices. */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    /** Parameter name shared by tenant-scoped query declarations. */
    String HOTEL_ID_PARAMETER = "hotelId";

    /**
     * Groups active customer-owned devices into the tenant's observed model catalog.
     *
     * @param hotelId tenant identifier
     * @return grouped catalog entries for the tenant
     */
    @Query("select new com.hotelpms.guest.dto.response.DeviceCatalogEntry("
        + "min(d.category), min(coalesce(d.manufacturer, '')), min(coalesce(d.model, '')), count(d.id)) "
        + "from Device d where d.hotelId = :hotelId "
        + "group by lower(trim(d.category)), lower(coalesce(trim(d.manufacturer), '')), "
        + "lower(coalesce(trim(d.model), '')) "
        + "order by count(d.id) desc")
    List<DeviceCatalogEntry> catalogByHotelId(@Param(HOTEL_ID_PARAMETER) UUID hotelId);

    /**
     * Counts active devices with the normalized catalog identity.
     *
     * @param hotelId tenant identifier
     * @param category device category
     * @param manufacturer device manufacturer
     * @param model device model
     * @return number of matching active devices
     */
    @Query("select count(d.id) from Device d where d.hotelId = :hotelId and d.active = true and "
        + "lower(trim(d.category)) = lower(trim(:category)) and "
        + "lower(coalesce(trim(d.manufacturer), '')) = lower(trim(:manufacturer)) and "
        + "lower(coalesce(trim(d.model), '')) = lower(trim(:model))")
    long countCatalogIdentity(@Param(HOTEL_ID_PARAMETER) UUID hotelId,
        @Param("category") String category, @Param("manufacturer") String manufacturer,
        @Param("model") String model);

    /**
     * Updates active tenant devices from one catalog identity to another.
     *
     * @param hotelId tenant identifier
     * @param sourceCategory source category
     * @param sourceManufacturer source manufacturer
     * @param sourceModel source model
     * @param targetCategory target category
     * @param targetManufacturer target manufacturer
     * @param targetModel target model
     * @param updatedAt update timestamp
     * @return number of normalized devices
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Device d set d.category = :targetCategory, d.manufacturer = :targetManufacturer, "
        + "d.model = :targetModel, d.updatedAt = :updatedAt where d.hotelId = :hotelId and d.active = true and "
        + "lower(trim(d.category)) = lower(trim(:sourceCategory)) and "
        + "lower(coalesce(trim(d.manufacturer), '')) = lower(trim(:sourceManufacturer)) and "
        + "lower(coalesce(trim(d.model), '')) = lower(trim(:sourceModel))")
    int normalizeCatalogIdentity(@Param(HOTEL_ID_PARAMETER) UUID hotelId,
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
    Optional<Device> findByIdAndHotelIdAndGuestId(
        UUID id, UUID hotelId, UUID guestId);

    /**
     * Lists devices for a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @return matching devices.
     */
    List<Device> findAllByHotelIdAndGuestId(UUID hotelId, UUID guestId);

    /**
     * Searches device identity fields within the active tenant.
     *
     * @param hotelId tenant identifier
     * @param query search text
     * @param pageable page and sorting criteria
     * @return matching tenant devices
     */
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
    Optional<Device> findByHotelIdAndGuestIdAndSerialNumber(
        UUID hotelId, UUID guestId, String serialNumber);

    /**
     * Finds a device by IMEI within a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @param imei IMEI value.
     * @return matching device.
     */
    Optional<Device> findByHotelIdAndGuestIdAndImei(
        UUID hotelId, UUID guestId, String imei);

    /**
     * Checks if a tenant customer already has a device with the serial number.
     *
     * @param hotelId tenant identifier
     * @param guestId customer identifier
     * @param serialNumber serial number
     * @return whether a matching device exists
     */
    boolean existsByHotelIdAndGuestIdAndSerialNumberIgnoreCase(
        UUID hotelId, UUID guestId, String serialNumber);

    /**
     * Checks for a duplicate serial number excluding the device being updated.
     *
     * @param hotelId tenant identifier
     * @param guestId customer identifier
     * @param serialNumber serial number
     * @param id device identifier to exclude
     * @return whether another matching device exists
     */
    boolean existsByHotelIdAndGuestIdAndSerialNumberIgnoreCaseAndIdNot(
        UUID hotelId, UUID guestId, String serialNumber, UUID id);

    /**
     * Checks if a tenant already has a device with the IMEI.
     *
     * @param hotelId tenant identifier
     * @param imei IMEI value
     * @return whether a matching device exists
     */
    boolean existsByHotelIdAndImeiIgnoreCase(UUID hotelId, String imei);

    /**
     * Checks for a duplicate IMEI excluding the device being updated.
     *
     * @param hotelId tenant identifier
     * @param imei IMEI value
     * @param id device identifier to exclude
     * @return whether another matching device exists
     */
    boolean existsByHotelIdAndImeiIgnoreCaseAndIdNot(
        UUID hotelId, String imei, UUID id);
}
