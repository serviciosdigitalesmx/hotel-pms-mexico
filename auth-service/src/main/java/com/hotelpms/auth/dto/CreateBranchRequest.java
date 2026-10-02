package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a branch under the authenticated tenant.
 * The tenant is never accepted from the client; it comes from
 * {@code X-Auth-Hotel}.
 *
 * @param name branch display name
 */
public record CreateBranchRequest(
        @NotBlank @Size(max = 120) String name) {
}
