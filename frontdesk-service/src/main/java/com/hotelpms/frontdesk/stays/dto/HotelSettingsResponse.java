package com.hotelpms.frontdesk.stays.dto;

import java.util.UUID;

/**
 * Response DTO for hotel settings and profile.
 *
 * <p>The Alloggiati password and WsKey are write-only and intentionally absent
 * here — only the non-secret username and a boolean summarizing whether
 * hotel-specific credentials are configured are ever returned.
 *
 * @param hotelId                         the hotel identifier
 * @param alloggiatiAutoSend              whether automatic Alloggiati submission is enabled
 * @param hotelName                       display name of the hotel property
 * @param address                         full street address including civic number
 * @param vatNumber                       Partita IVA — Italian VAT number
 * @param fiscalCode                      Codice Fiscale — Italian fiscal code
 * @param logoUrl                         URL of the hotel logo image
 * @param alloggiatiUsername              Alloggiati Web portal username for this hotel, if configured
 * @param alloggiatiCredentialsConfigured whether this hotel has its own Alloggiati Web
 *                                        credentials (username + password + WsKey all set),
 *                                        as opposed to falling back to the global ones
 * @param sendReservationConfirmedEmail   whether the guest is emailed when a reservation is created
 * @param sendCheckoutEmail               whether the guest is emailed a stay summary at check-out
 * @param emailSubjectReservationConfirmed custom subject line for the reservation-confirmed
 *                                        email, or {@code null} if using the default
 * @param emailSubjectCheckout            custom subject line for the checkout email,
 *                                        or {@code null} if using the default
 * @param emailGreetingText               greeting/signature line appended to every
 *                                        transactional email footer, or {@code null} if unset
 * @param cap                             CAP — Italian 5-digit postal code, or {@code null} if unset
 * @param comune                          Comune — municipality name, or {@code null} if unset
 * @param provincia                       Provincia — 2-letter province code, or {@code null} if unset
 * @param city                            city or municipality used for the hotel profile
 * @param state                           state or province used for the hotel profile
 * @param country                         country used for the hotel profile
 * @param postalCode                      postal code used for the hotel profile
 * @param currency                        ISO 4217 currency code for the hotel
 * @param locale                          locale used for formatting and translations
 * @param timezone                        IANA timezone used for hotel operations
 * @param publicSlug                      public booking slug for the hotel
 * @param aiEnabled                       whether the tenant AI assistant is enabled
 * @param aiModel                         provider model identifier selected by the hotel
 * @param aiApiKeyConfigured              whether an encrypted provider key exists
 * @param aiInstructions                  hotel-specific assistant instructions
 */
public record HotelSettingsResponse(
        UUID hotelId,
        boolean alloggiatiAutoSend,
        String hotelName,
        String address,
        String vatNumber,
        String fiscalCode,
        String logoUrl,
        String alloggiatiUsername,
        boolean alloggiatiCredentialsConfigured,
        boolean sendReservationConfirmedEmail,
        boolean sendCheckoutEmail,
        String emailSubjectReservationConfirmed,
        String emailSubjectCheckout,
        String emailGreetingText,
        String cap,
        String comune,
        String provincia,
        String city,
        String state,
        String country,
        String postalCode,
        String currency,
        String locale,
        String timezone,
        String publicSlug,
        boolean aiEnabled,
        String aiModel,
        boolean aiApiKeyConfigured,
        String aiInstructions) {

    /**
     * Backward-compatible constructor for callers that do not provide the new profile fields.
     *
     * @param hotelId hotel identifier
     * @param alloggiatiAutoSend whether automatic reporting is enabled
     * @param hotelName display name of the hotel
     * @param address hotel address
     * @param vatNumber tax identifier
     * @param fiscalCode fiscal identifier
     * @param logoUrl logo URL
     * @param alloggiatiUsername reporting username
     * @param alloggiatiCredentialsConfigured whether reporting credentials are configured
     * @param sendReservationConfirmedEmail whether reservation emails are enabled
     * @param sendCheckoutEmail whether checkout emails are enabled
     * @param emailSubjectReservationConfirmed reservation email subject
     * @param emailSubjectCheckout checkout email subject
     * @param emailGreetingText email greeting text
     * @param cap postal code
     * @param comune municipality
     * @param provincia province code
     */
    public HotelSettingsResponse(
            final UUID hotelId,
            final boolean alloggiatiAutoSend,
            final String hotelName,
            final String address,
            final String vatNumber,
            final String fiscalCode,
            final String logoUrl,
            final String alloggiatiUsername,
            final boolean alloggiatiCredentialsConfigured,
            final boolean sendReservationConfirmedEmail,
            final boolean sendCheckoutEmail,
            final String emailSubjectReservationConfirmed,
            final String emailSubjectCheckout,
            final String emailGreetingText,
            final String cap,
            final String comune,
            final String provincia) {
        this(hotelId, alloggiatiAutoSend, hotelName, address, vatNumber, fiscalCode, logoUrl,
                alloggiatiUsername, alloggiatiCredentialsConfigured, sendReservationConfirmedEmail,
                sendCheckoutEmail, emailSubjectReservationConfirmed, emailSubjectCheckout,
                emailGreetingText, cap, comune, provincia, null, null, null, null,
                "MXN", "es-MX", "America/Monterrey", null,
                false, "qwen3:4b-instruct-2507-q4_K_M", false, null);
    }
}
