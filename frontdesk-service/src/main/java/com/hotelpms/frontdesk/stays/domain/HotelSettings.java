package com.hotelpms.frontdesk.stays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-hotel operational settings. One row per hotel; hotel_id is the natural primary key.
 */
@Entity
@Table(name = "hotel_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class HotelSettings {

    private static final int LEN_HOTEL_NAME = 150;
    private static final int LEN_ADDRESS = 200;
    private static final int LEN_FISCAL = 20;
    private static final int LEN_LOGO_URL = 500;
    private static final int LEN_ALLOGGIATI_USERNAME = 100;
    private static final int LEN_EMAIL_SUBJECT = 200;
    private static final int LEN_EMAIL_GREETING = 300;
    private static final int LEN_CAP = 5;
    private static final int LEN_PROVINCIA = 2;
    private static final int LEN_COMUNE = 100;
    private static final int LEN_COUNTRY = 100;
    private static final int LEN_CURRENCY = 3;
    private static final int LEN_LOCALE = 20;
    private static final int LEN_TIMEZONE = 50;
    private static final int LEN_SLUG = 120;
    private static final int LEN_AI_MODEL = 150;
    private static final int LEN_AI_INSTRUCTIONS = 1000;

    /** The hotel this settings row belongs to (primary key). */
    @Id
    @Column(name = "hotel_id")
    private UUID hotelId;

    /**
     * When {@code true}, the Alloggiati Web report is automatically submitted to the
     * Polizia di Stato portal at each check-in.
     */
    @Column(name = "alloggiati_auto_send", nullable = false)
    private boolean alloggiatiAutoSend;

    /** Display name of the hotel property (e.g. "Hotel Bella Vista"). */
    @Column(name = "hotel_name", length = LEN_HOTEL_NAME)
    private String hotelName;

    /** Street address including civic number (e.g. "Via Roma 12"). */
    @Column(name = "address", length = LEN_ADDRESS)
    private String address;

    /** Partita IVA — Italian VAT number (11 digits). */
    @Column(name = "vat_number", length = LEN_FISCAL)
    private String vatNumber;

    /** Codice Fiscale — Italian fiscal code. */
    @Column(name = "fiscal_code", length = LEN_FISCAL)
    private String fiscalCode;

    /** Optional URL of the hotel logo image. */
    @Column(name = "logo_url", length = LEN_LOGO_URL)
    private String logoUrl;

    /** CAP — Italian 5-digit postal code, required for a valid FatturaPA {@code Sede}. */
    @Column(name = "cap", length = LEN_CAP)
    private String cap;

    /**
     * Comune — municipality name, validated against the Portale Alloggiati Web
     * {@code alloggiati_comuni} reference table together with {@link #provincia}.
     */
    @Column(name = "comune", length = LEN_COMUNE)
    private String comune;

    /** Provincia — 2-letter province code (e.g. {@code "RM"}). */
    @Column(name = "provincia", length = LEN_PROVINCIA)
    private String provincia;

    @Column(name = "city", length = LEN_COMUNE)
    private String city;

    @Column(name = "state", length = LEN_COMUNE)
    private String state;

    @Column(name = "country", length = LEN_COUNTRY)
    private String country;

    @Column(name = "postal_code", length = LEN_CAP)
    private String postalCode;

    @Column(name = "currency", length = LEN_CURRENCY, nullable = false)
    @Builder.Default
    private String currency = "MXN";

    @Column(name = "locale", length = LEN_LOCALE, nullable = false)
    @Builder.Default
    private String locale = "es-MX";

    @Column(name = "timezone", length = LEN_TIMEZONE, nullable = false)
    @Builder.Default
    private String timezone = "America/Monterrey";

    @Column(name = "public_slug", length = LEN_SLUG, unique = true)
    private String publicSlug;

    /** Whether the tenant-scoped AI assistant is available to operational users. */
    @Column(name = "ai_enabled", nullable = false)
    private boolean aiEnabled;

    /** OpenAI-compatible provider model selected by this hotel. */
    @Column(name = "ai_model", length = LEN_AI_MODEL, nullable = false)
    @Builder.Default
    private String aiModel = "qwen3:4b-instruct-2507-q4_K_M";

    /** Optional hotel-specific instructions appended to the assistant policy. */
    @Column(name = "ai_instructions", length = LEN_AI_INSTRUCTIONS)
    private String aiInstructions;

    /** AI provider API key encrypted at rest and never exposed by an API response. */
    @Column(name = "ai_api_key_encrypted")
    private String aiApiKeyEncrypted;

    /**
     * Per-hotel Alloggiati Web portal username. Not a secret on its own (a portal
     * login name), so it is stored and returned in plain text.
     */
    @Column(name = "alloggiati_username", length = LEN_ALLOGGIATI_USERNAME)
    private String alloggiatiUsername;

    /**
     * Per-hotel Alloggiati Web portal password, AES-GCM encrypted via
     * {@link com.hotelpms.frontdesk.stays.security.AlloggiatiCredentialEncryptor}.
     * Never exposed in any response DTO.
     */
    @Column(name = "alloggiati_password_encrypted")
    private String alloggiatiPasswordEncrypted;

    /**
     * Per-hotel Alloggiati Web WsKey, AES-GCM encrypted via
     * {@link com.hotelpms.frontdesk.stays.security.AlloggiatiCredentialEncryptor}.
     * Never exposed in any response DTO.
     */
    @Column(name = "alloggiati_ws_key_encrypted")
    private String alloggiatiWsKeyEncrypted;

    /**
     * When {@code true} (default), a confirmation email is sent to the guest when a
     * reservation is created.
     */
    @Column(name = "send_reservation_confirmed_email", nullable = false)
    @Builder.Default
    private boolean sendReservationConfirmedEmail = true;

    /**
     * When {@code true} (default), a stay-summary email with invoice detail is sent
     * to the guest at check-out.
     */
    @Column(name = "send_checkout_email", nullable = false)
    @Builder.Default
    private boolean sendCheckoutEmail = true;

    /**
     * Optional per-hotel override of the reservation-confirmed email subject line.
     * When blank/null, notification-service falls back to its default IT/EN subject.
     */
    @Column(name = "email_subject_reservation_confirmed", length = LEN_EMAIL_SUBJECT)
    private String emailSubjectReservationConfirmed;

    /**
     * Optional per-hotel override of the checkout email subject line.
     * When blank/null, notification-service falls back to its default IT/EN subject.
     */
    @Column(name = "email_subject_checkout", length = LEN_EMAIL_SUBJECT)
    private String emailSubjectCheckout;

    /**
     * Optional per-hotel greeting/signature line appended to the footer of every
     * transactional email (e.g. "Vi aspettiamo, il team di Hotel Bella Vista").
     */
    @Column(name = "email_greeting_text", length = LEN_EMAIL_GREETING)
    private String emailGreetingText;

    /** The timestamp when the record was created. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** The timestamp when the record was last updated. */
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    /**
     * Whether this hotel has fully configured its own Alloggiati Web credentials
     * (username + password + WsKey all present), as opposed to relying on the
     * global instance-wide fallback credentials.
     *
     * @return {@code true} if all three fields are non-blank
     */
    public boolean hasAlloggiatiCredentials() {
        return isNotBlank(alloggiatiUsername)
                && isNotBlank(alloggiatiPasswordEncrypted)
                && isNotBlank(alloggiatiWsKeyEncrypted);
    }

    /**
     * @return whether this hotel has an encrypted AI provider key configured
     */
    public boolean hasAiApiKey() {
        return isNotBlank(aiApiKeyEncrypted);
    }

    private static boolean isNotBlank(final String value) {
        return value != null && !value.isBlank();
    }
}
