package com.hotelpms.guest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for guest read operations.
 *
 * @param id                the unique ID of the guest
 * @param firstName         the first name of the guest
 * @param lastName          the last name of the guest
 * @param email             the email address of the guest
 * @param phone             the phone number of the guest
 * @param address           the street address of the guest
 * @param city              the city of the guest
 * @param country           the country of the guest
 * @param dateOfBirth       the date of birth of the guest
 * @param fiscalCode        Italian Codice Fiscale (optional)
 * @param vatNumber         Partita IVA / VAT number (optional)
 * @param companyName       company / legal entity name (optional)
 * @param sdiCode           SDI/Destinatario code (optional)
 * @param pecEmail          PEC email (optional)
 * @param cap               CAP — Italian 5-digit postal code (optional)
 * @param comune            Comune — municipality name (optional)
 * @param provincia         Provincia — 2-letter province code (optional)
 * @param rfc               Mexican RFC (optional)
 * @param fiscalName        fiscal business name (optional)
 * @param fiscalPostalCode  fiscal postal code (optional)
 * @param fiscalRegime      fiscal regime code (optional)
 * @param cfdiUse           CFDI usage code (optional)
 * @param billingEmail      separate billing email (optional)
 * @param identityDocuments identity documents belonging to the guest
 * @param gdprConsentDate   date GDPR consent was recorded
 * @param createdAt         creation timestamp
 * @param updatedAt         last update timestamp
 */
@SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public record GuestResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String country,
        LocalDate dateOfBirth,

        String rfc,
        String fiscalName,
        String fiscalPostalCode,
        String fiscalRegime,
        String cfdiUse,
        String billingEmail,

        String fiscalCode,
        String vatNumber,
        String companyName,
        String sdiCode,
        String pecEmail,
        String cap,
        String comune,
        String provincia,
        List<IdentityDocumentResponseDTO> identityDocuments,
        LocalDate gdprConsentDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * Compact constructor — defensively copies the mutable list on construction.
     */
    public GuestResponse {
        if (identityDocuments != null) {
            identityDocuments = List.copyOf(identityDocuments);
        }
    }

    /**
     * Returns a defensive copy of the identity documents list.
     *
     * @return an unmodifiable copy of the identity documents list, or {@code null}
     *         if no documents are present
     */
    @Override
    public List<IdentityDocumentResponseDTO> identityDocuments() {
        return identityDocuments == null ? null : List.copyOf(identityDocuments);
    }
}
