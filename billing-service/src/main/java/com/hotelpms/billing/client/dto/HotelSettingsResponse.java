package com.hotelpms.billing.client.dto;

import java.util.UUID;

/**
 * External DTO for hotel profile data fetched from stay-service.
 *
 * @param hotelId    the hotel identifier
 * @param hotelName  display name of the hotel property
 * @param address    full street address
 * @param vatNumber  Partita IVA — Italian VAT number
 * @param fiscalCode Codice Fiscale — Italian fiscal code
 * @param logoUrl    optional URL of the hotel logo image
 * @param cap        CAP — Italian 5-digit postal code, or {@code null} if unset
 * @param comune     Comune — municipality name, or {@code null} if unset
 * @param provincia  Provincia — 2-letter province code, or {@code null} if unset
 * @param currency   ISO 4217 currency configured for this hotel
 */
public record HotelSettingsResponse(
        UUID hotelId,
        String hotelName,
        String address,
        String vatNumber,
        String fiscalCode,
        String logoUrl,
        String cap,
        String comune,
        String provincia,
        String currency) {

    /** Backward-compatible constructor for existing callers and tests. */
    public HotelSettingsResponse(
            final UUID hotelId,
            final String hotelName,
            final String address,
            final String vatNumber,
            final String fiscalCode,
            final String logoUrl,
            final String cap,
            final String comune,
            final String provincia) {
        this(hotelId, hotelName, address, vatNumber, fiscalCode, logoUrl, cap, comune, provincia, "MXN");
    }
}
