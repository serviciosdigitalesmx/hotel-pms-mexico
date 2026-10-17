package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a branch under the authenticated tenant. The tenant is never
 * accepted from the client; it comes from {@code X-Auth-Hotel}.
 *
 * @param name branch display name
 * @param phone optional branch phone
 * @param extraField optional business-specific field
 * @param address optional branch address
 * @param timeZone optional branch time zone
 * @param dateFormat optional date display format
 * @param country optional branch country
 * @param currency optional ISO currency code
 */
public record CreateBranchRequest(
    @NotBlank @Size(max = TenantBranchRequestConstraints.MAX_NAME_LENGTH) String name,
    @Size(max = TenantBranchRequestConstraints.MAX_PHONE_LENGTH) String phone,
    @Size(max = TenantBranchRequestConstraints.MAX_EXTRA_FIELD_LENGTH) String extraField,
    @Size(max = TenantBranchRequestConstraints.MAX_ADDRESS_LENGTH) String address,
    @Size(max = TenantBranchRequestConstraints.MAX_TIME_ZONE_LENGTH) String timeZone,
    @jakarta.validation.constraints.Pattern(regexp = "|mm/dd/yyyy|dd/mm/yyyy") String dateFormat,
    @Size(max = TenantBranchRequestConstraints.MAX_COUNTRY_LENGTH) String country,
    @jakarta.validation.constraints.Pattern(regexp = "|[A-Za-z]{3}") String currency) {
  /**
   * Builds a request that only changes the branch name.
   *
   * @param name branch display name
   */
  public CreateBranchRequest(final String name) {
    this(name, null, null, null, null, null, null, null);
  }
}
