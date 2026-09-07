package com.hotelpms.guest.dto.request;

import com.hotelpms.guest.util.ValidationConstants;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for guest creation and update operations.
 *
 * @param firstName         the first name of the guest
 * @param lastName          the last name of the guest
 * @param email             email address of the guest (required if phone absent)
 * @param phone             phone number of the guest (required if email absent)
 * @param address           street address of the guest
 * @param city              city of the guest
 * @param country           country of the guest
 * @param dateOfBirth       date of birth of the guest
 * @param rfc               Mexican RFC (optional)
 * @param fiscalName        fiscal business name (optional)
 * @param fiscalPostalCode  fiscal postal code (optional)
 * @param fiscalRegime      fiscal regime code (optional)
 * @param cfdiUse           CFDI usage code (optional)
 * @param billingEmail      separate billing email (optional)
 * @param fiscalCode        Italian Codice Fiscale or equivalent (optional)
 * @param vatNumber         Partita IVA / VAT number (optional)
 * @param companyName       company / legal entity name (optional)
 * @param sdiCode           SDI/Destinatario code for electronic invoicing (optional)
 * @param pecEmail          PEC email for electronic invoicing (optional)
 * @param cap               CAP — Italian 5-digit postal code (optional)
 * @param comune            Comune — municipality name (optional)
 * @param provincia         Provincia — 2-letter province code (optional)
 */
public record GuestRequest(
        @NotBlank
        @Size(max = ValidationConstants.MAX_FIRST_NAME_LENGTH)
        @Pattern(regexp = ValidationConstants.NAME_PATTERN)
        String firstName,
        @NotBlank
        @Size(max = ValidationConstants.MAX_LAST_NAME_LENGTH)
        @Pattern(regexp = ValidationConstants.NAME_PATTERN)
        String lastName,
        @Email @Size(max = ValidationConstants.MAX_EMAIL_LENGTH) String email,
        @Size(max = ValidationConstants.MAX_PHONE_LENGTH)
        @Pattern(regexp = ValidationConstants.PHONE_PATTERN)
        String phone,
        @Size(max = ValidationConstants.MAX_ADDRESS_LENGTH)
        @Pattern(regexp = ValidationConstants.TEXT_SAFE_PATTERN)
        String address,
        @Size(max = ValidationConstants.MAX_LOCATION_LENGTH)
        @Pattern(regexp = ValidationConstants.LOCATION_PATTERN)
        String city,
        @Size(max = ValidationConstants.MAX_LOCATION_LENGTH)
        @Pattern(regexp = ValidationConstants.LOCATION_PATTERN)
        String country,
        @Past LocalDate dateOfBirth,
        @Size(max = ValidationConstants.MAX_RFC_LENGTH)
        @Pattern(
                regexp = "^$|^[A-Za-zÑñ&]{3,4}[0-9]{6}[A-Za-z0-9]{3}$",
                message = "RFC_INVALID")
        String rfc,

        @Size(max = ValidationConstants.MAX_FISCAL_NAME_LENGTH)
        String fiscalName,

        @Pattern(
                regexp = "^$|^[0-9]{5}$",
                message = "FISCAL_POSTAL_CODE_INVALID")
        String fiscalPostalCode,

        @Pattern(
                regexp = "^$|^[0-9]{3}$",
                message = "FISCAL_REGIME_INVALID")
        String fiscalRegime,

        @Pattern(
                regexp = "^$|^[A-Za-z0-9]{3,4}$",
                message = "CFDI_USE_INVALID")
        String cfdiUse,

        @Email
        @Size(max = ValidationConstants.MAX_EMAIL_LENGTH)
        String billingEmail,

        /*
         * Legacy Italia — temporary compatibility with billing-service.
         */
        @Size(max = ValidationConstants.MAX_FISCAL_CODE_LENGTH)
        @Pattern(regexp = ValidationConstants.FISCAL_CODE_PATTERN)
        String fiscalCode,
        @Size(max = ValidationConstants.MAX_VAT_NUMBER_LENGTH)
        @Pattern(regexp = ValidationConstants.VAT_NUMBER_PATTERN)
        String vatNumber,
        @Size(max = ValidationConstants.MAX_COMPANY_NAME_LENGTH)
        @Pattern(regexp = ValidationConstants.TEXT_SAFE_PATTERN)
        String companyName,
        @Size(max = ValidationConstants.MAX_SDI_CODE_LENGTH)
        @Pattern(regexp = ValidationConstants.SDI_CODE_PATTERN)
        String sdiCode,
        @Email @Size(max = ValidationConstants.MAX_EMAIL_LENGTH)
        String pecEmail,
        @Pattern(regexp = "^$|\\d{5}", message = "CAP must be 5 digits") String cap,
        @Size(max = 100) String comune,
        @Pattern(regexp = "^$|[A-Za-z]{2}", message = "Provincia must be 2 letters") String provincia) {

    /**
     * Validates that at least one of email or phone is provided.
     *
     * @return true if email or phone is present and non-blank
     */
    @AssertTrue(message = "At least one of email or phone must be provided")
    public boolean isEmailOrPhoneProvided() {
        final boolean hasEmail = email != null && !email.isBlank();
        final boolean hasPhone = phone != null && !phone.isBlank();
        return hasEmail || hasPhone;
    }

    /**
     * Requires a complete CFDI profile once any fiscal field is supplied.
     *
     * @return true when no fiscal field is supplied or the profile is complete
     */
    @AssertTrue(message = "CFDI_PROFILE_INCOMPLETE")
    public boolean isCfdiProfileComplete() {
        final boolean anyCfdi =
                notBlank(rfc)
                || notBlank(fiscalName)
                || notBlank(fiscalPostalCode)
                || notBlank(fiscalRegime)
                || notBlank(cfdiUse);

        final boolean completeProfile = notBlank(rfc)
                && notBlank(fiscalName)
                && notBlank(fiscalPostalCode)
                && notBlank(fiscalRegime)
                && notBlank(cfdiUse);
        return !anyCfdi || completeProfile;
    }

    private static boolean notBlank(final String value) {
        return value != null && !value.isBlank();
    }
}
