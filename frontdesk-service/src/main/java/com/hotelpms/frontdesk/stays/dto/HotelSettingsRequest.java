package com.hotelpms.frontdesk.stays.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating hotel settings and profile.
 *
 * <p>All fields are treated as a partial patch: a {@code null} value (i.e. the field is
 * absent from the JSON body) means "leave the currently stored value unchanged" — it is
 * never interpreted as "clear this field". This lets callers such as a single settings
 * toggle send only the field they changed without wiping out the rest of the hotel
 * profile. Boolean flags therefore use the boxed {@link Boolean} type rather than the
 * primitive {@code boolean}, since Jackson cannot otherwise distinguish "absent" from
 * "explicitly false".
 *
 * @param alloggiatiAutoSend             whether to automatically submit the Alloggiati
 *                                        report at check-in ({@code null} = unchanged)
 * @param hotelName                      display name of the hotel property (optional)
 * @param address                        full street address including civic number (optional)
 * @param vatNumber                      Partita IVA — Italian VAT number (optional)
 * @param fiscalCode                     Codice Fiscale — Italian fiscal code (optional)
 * @param logoUrl                        URL of the hotel logo image (optional)
 * @param alloggiatiUsername             Alloggiati Web portal username for this hotel (optional —
 *                                        falls back to the global instance credentials when absent)
 * @param alloggiatiPassword             Alloggiati Web portal password, write-only: blank/null means
 *                                        "leave the currently stored password unchanged", it is never
 *                                        sent back by the API to be cleared by omission
 * @param alloggiatiWsKey                Alloggiati Web WsKey, same write-only semantics as the password
 * @param sendReservationConfirmedEmail  whether to email the guest when a reservation is created
 *                                        ({@code null} = unchanged)
 * @param sendCheckoutEmail              whether to email the guest a stay summary at check-out
 *                                        ({@code null} = unchanged)
 * @param emailSubjectReservationConfirmed optional custom subject line for the reservation-confirmed
 *                                        email; blank/null falls back to the default IT/EN subject
 * @param emailSubjectCheckout           optional custom subject line for the checkout email;
 *                                        blank/null falls back to the default IT/EN subject
 * @param emailGreetingText              optional greeting/signature line appended to every
 *                                        transactional email footer (optional)
 * @param cap                            CAP — Italian 5-digit postal code (optional; required
 *                                        only to export a valid FatturaPA XML)
 * @param comune                         Comune — municipality name, validated together with
 *                                        {@code provincia} against the Alloggiati Web reference
 *                                        data (optional; required only to export FatturaPA)
 * @param provincia                      Provincia — 2-letter province code, e.g. {@code "RM"}
 *                                        (optional; required only to export FatturaPA)
 * @param city                           City name (optional)
 * @param state                          State/region name (optional)
 * @param country                        Country name (optional)
 * @param postalCode                     Postal code (optional)
 * @param currency                       3-letter ISO currency code (optional)
 * @param locale                         Locale identifier, e.g. {@code "es-MX"} (optional)
 * @param timezone                       Timezone identifier, e.g. {@code "America/Monterrey"} (optional)
 * @param publicSlug                     public booking slug for the hotel (optional)
 * @param aiEnabled                      whether the tenant AI assistant is enabled
 * @param aiModel                        provider model identifier selected by the hotel
 * @param aiApiKey                       write-only provider API key; blank means unchanged
 * @param aiInstructions                 hotel-specific assistant instructions
 */
public record HotelSettingsRequest(
        Boolean alloggiatiAutoSend,
        String hotelName,
        String address,
        String vatNumber,
        String fiscalCode,
        @Size(max = MAX_LOGO_URL_LENGTH) @Pattern(regexp = "^$|https?://.+", message = "logoUrl must be a valid http(s) URL")
                String logoUrl,
        @Size(max = 100) String alloggiatiUsername,
        @Size(max = MAX_CREDENTIAL_LENGTH) String alloggiatiPassword,
        @Size(max = MAX_CREDENTIAL_LENGTH) String alloggiatiWsKey,
        Boolean sendReservationConfirmedEmail,
        Boolean sendCheckoutEmail,
        @Size(max = MAX_SUBJECT_LENGTH) String emailSubjectReservationConfirmed,
        @Size(max = MAX_SUBJECT_LENGTH) String emailSubjectCheckout,
        @Size(max = MAX_GREETING_LENGTH) String emailGreetingText,
        @Pattern(regexp = "^$|\\d{5}", message = "CAP must be 5 digits") String cap,
        @Size(max = 100) String comune,
        @Pattern(regexp = "^$|[A-Za-z]{2}", message = "Provincia must be 2 letters") String provincia,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Pattern(regexp = "^$|\\d{5}", message = "Postal code must be 5 digits") String postalCode,
        @Size(max = 3) String currency,
        @Size(max = MAX_LOCALE_LENGTH) String locale,
        @Size(max = MAX_TIMEZONE_LENGTH) String timezone,
        @Size(max = MAX_PUBLIC_SLUG_LENGTH) String publicSlug,
        Boolean aiEnabled,
        @Size(max = MAX_AI_MODEL_LENGTH) String aiModel,
        @Size(max = MAX_CREDENTIAL_LENGTH) String aiApiKey,
        @Size(max = MAX_AI_INSTRUCTIONS_LENGTH) String aiInstructions) {

    /** Maximum length accepted for the hotel logo URL — matches HotelSettings.LEN_LOGO_URL. */
    public static final int MAX_LOGO_URL_LENGTH = 500;

    /** Maximum length accepted for the Alloggiati Web password/WsKey fields. */
    public static final int MAX_CREDENTIAL_LENGTH = 200;

    /** Maximum length accepted for the custom email subject fields. */
    public static final int MAX_SUBJECT_LENGTH = 200;

    /** Maximum length accepted for the email greeting/signature field. */
    public static final int MAX_GREETING_LENGTH = 300;

    /** Maximum length accepted for the locale field. */
    public static final int MAX_LOCALE_LENGTH = 20;

    /** Maximum length accepted for the timezone field. */
    public static final int MAX_TIMEZONE_LENGTH = 50;

    /** Maximum length accepted for the public booking slug. */
    public static final int MAX_PUBLIC_SLUG_LENGTH = 120;

    /** Maximum length accepted for the AI model identifier. */
    public static final int MAX_AI_MODEL_LENGTH = 150;

    /** Maximum length accepted for tenant-specific assistant instructions. */
    public static final int MAX_AI_INSTRUCTIONS_LENGTH = 1000;

    /**
     * Backward-compatible constructor for existing callers that only update the
     * original notification and Italian-integration fields.
     */
    public HotelSettingsRequest(
            final Boolean alloggiatiAutoSend,
            final String hotelName,
            final String address,
            final String vatNumber,
            final String fiscalCode,
            final String logoUrl,
            final String alloggiatiUsername,
            final String alloggiatiPassword,
            final String alloggiatiWsKey,
            final Boolean sendReservationConfirmedEmail,
            final Boolean sendCheckoutEmail,
            final String emailSubjectReservationConfirmed,
            final String emailSubjectCheckout,
            final String emailGreetingText,
            final String cap,
            final String comune,
            final String provincia) {
        this(alloggiatiAutoSend, hotelName, address, vatNumber, fiscalCode, logoUrl,
                alloggiatiUsername, alloggiatiPassword, alloggiatiWsKey,
                sendReservationConfirmedEmail, sendCheckoutEmail,
                emailSubjectReservationConfirmed, emailSubjectCheckout, emailGreetingText,
                cap, comune, provincia, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
