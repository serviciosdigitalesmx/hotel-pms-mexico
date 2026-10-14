package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a branch under the authenticated tenant. The tenant is never
 * accepted from the client; it comes from {@code X-Auth-Hotel}.
 *
 * @param name branch display name
 */
public record CreateBranchRequest(
    @NotBlank @Size(max = TenantBranchRequestConstraints.MAX_NAME_LENGTH) String name,
    @Size(max = 40) String phone,
    @Size(max = 200) String extraField,
    @Size(max = 500) String address,
    @Size(max = 80) String timeZone,
    @jakarta.validation.constraints.Pattern(regexp = "|mm/dd/yyyy|dd/mm/yyyy") String dateFormat,
    @Size(max = 80) String country,
    @jakarta.validation.constraints.Pattern(regexp = "|[A-Za-z]{3}") String currency) {
  public CreateBranchRequest(final String name) {
    this(name, null, null, null, null, null, null, null);
  }
}
