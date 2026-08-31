package com.hotelpms.frontdesk.client.dto;

/**
 * Guest creation contract used by frontdesk-service.
 *
 * CFDI fields are optional for normal hotel operation.
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
