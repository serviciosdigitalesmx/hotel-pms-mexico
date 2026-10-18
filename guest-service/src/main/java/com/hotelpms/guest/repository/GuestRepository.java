package com.hotelpms.guest.repository;

import com.hotelpms.internalauth.architecture.TenantScopeExempt;
import com.hotelpms.guest.model.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Guest entity.
 *
 * <p>All query methods are scoped to {@code hotelId} to enforce
 * multi-tenant isolation (T-GST-01, T-GST-03). Direct use of the
 * inherited {@code findById} / {@code findAll} is intentionally avoided
 * in the service layer; hotel-scoped variants must be used instead.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID>, JpaSpecificationExecutor<Guest> {

    /**
     * Finds an active guest by UUID within the given hotel.
     * Returns {@link Optional#empty()} if the guest exists but belongs to a
     * different hotel, preventing IDOR enumeration.
     *
     * @param id      the guest UUID
     * @param hotelId the owning hotel UUID
     * @return the guest if found and owned by the hotel
     */
    Optional<Guest> findByIdAndHotelId(final UUID id, final UUID hotelId);

    /**
     * Finds the first guest matching email in a hotel.
     *
     * @param hotelId tenant identifier.
     * @param email email value
     * @return matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndEmailIgnoreCase(final UUID hotelId, final String email);

    /**
     * Finds a guest by email while excluding the profile currently being edited.
     *
     * @param hotelId tenant identifier
     * @param email email value
     * @param id customer identifier to exclude
     * @return another matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndEmailIgnoreCaseAndIdNot(
            final UUID hotelId, final String email, final UUID id);

    /**
     * Finds the first guest matching phone in a hotel.
     *
     * @param hotelId tenant identifier.
     * @param phone phone value
     * @return matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndPhone(final UUID hotelId, final String phone);

    /**
     * Finds a guest by phone while excluding the profile currently being edited.
     *
     * @param hotelId tenant identifier
     * @param phone phone value
     * @param id customer identifier to exclude
     * @return another matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndPhoneAndIdNot(
            final UUID hotelId, final String phone, final UUID id);

    /**
     * Finds the first guest matching RFC in a hotel.
     *
     * @param hotelId tenant identifier.
     * @param rfc RFC value
     * @return matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndRfcIgnoreCase(final UUID hotelId, final String rfc);

    /**
     * Finds a guest by RFC while excluding the profile currently being edited.
     *
     * @param hotelId tenant identifier
     * @param rfc RFC value
     * @param id customer identifier to exclude
     * @return another matching guest, if present
     */
    Optional<Guest> findFirstByHotelIdAndRfcIgnoreCaseAndIdNot(
            final UUID hotelId, final String rfc, final UUID id);

    /**
     * Returns a paginated list of all active guests belonging to a hotel.
     *
     * @param hotelId  the owning hotel UUID
     * @param pageable pagination parameters
     * @return page of guests scoped to the hotel
     */
    Page<Guest> findAllByHotelId(final UUID hotelId, final Pageable pageable);

    /**
     * Returns all active guests with the given IDs that belong to the hotel.
     * IDs belonging to other hotels are silently excluded.
     *
     * @param ids     the list of guest UUIDs to look up
     * @param hotelId the owning hotel UUID
     * @return list of matching guests scoped to the hotel
     */
    List<Guest> findAllByIdInAndHotelId(final List<UUID> ids, final UUID hotelId);

    /**
     * Searches guests within a hotel by first name, last name, email or city
     * containing the provided value (case-insensitive).
     *
     * <p>The keyword is split on whitespace and every token must match at least one of
     * the four fields (BUG-2, {@code docs/LIVE_E2E_AUDIT_2026-07.md}) — a single LIKE
     * against the whole string never matched a "Nome Cognome" query, since neither field
     * alone contains both words. See {@link GuestSearchSpecifications}.
     *
     * @param keyword  search term matched against name, email, city; may contain multiple
     *                 whitespace-separated tokens
     * @param hotelId  the owning hotel UUID
     * @param pageable pagination information
     * @return a page of matching guests scoped to the hotel
     */
    default Page<Guest> searchByKeywordAndHotelId(
            final String keyword, final UUID hotelId, final Pageable pageable) {
        return findAll(GuestSearchSpecifications.matchingAllTokens(keyword, hotelId), pageable);
    }

    /**
     * Returns all active guests whose GDPR consent date is strictly before the
     * given cutoff. Used by the nightly retention job as a pre-filter to identify
     * candidates for anonymisation (T-GST-05).
     *
     * <p>The caller must still verify the legal-hold constraints (TULPS and
     * fiscal) before proceeding with anonymisation.
     *
     * @param cutoff the exclusive upper bound for the consent date
     * @return list of candidate guests
     */
    @TenantScopeExempt(reason = "Intentionally platform-wide: the nightly retention job scans "
            + "every hotel for candidates, then GuestRetentionJobServiceImpl verifies the "
            + "per-guest legal-hold constraints (TULPS/fiscal) before ever anonymising anything "
            + "(T-GST-05) — this is a pre-filter, not a data-returning endpoint.")
    List<Guest> findByGdprConsentDateBefore(final LocalDate cutoff);
}
