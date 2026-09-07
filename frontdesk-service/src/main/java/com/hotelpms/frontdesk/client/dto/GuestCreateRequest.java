package com.hotelpms.frontdesk.client.dto;

/**
 * Guest creation contract used by frontdesk-service.
 *
 * <p>CFDI fields are optional for normal hotel operation.
 *
 * @param firstName first name
 * @param lastName last name
 * @param email email address
 * @param phone phone number
 * @param address street address
 * @param city city
 * @param country country code
 * @param rfc Mexican tax identifier
 * @param fiscalName fiscal business name
 * @param fiscalPostalCode fiscal postal code
 * @param fiscalRegime fiscal regime
 * @param cfdiUse CFDI usage code
 * @param billingEmail separate billing email
 */
public record GuestCreateRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String rfc,
        String fiscalName,
        String fiscalPostalCode,
        String fiscalRegime,
        String cfdiUse,
        String billingEmail) {

    /**
     * Creates a guest with only the basic identity fields.
     *
     * @param firstName first name
     * @param lastName last name
     * @param email email address
     */
    public GuestCreateRequest(
            final String firstName,
            final String lastName,
            final String email) {

        this(
                firstName,
                lastName,
                email,
                null,
                null,
                null,
                "MX",
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
