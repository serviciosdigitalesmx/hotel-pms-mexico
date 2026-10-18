package com.hotelpms.internalauth.feign;

/**
 * Authenticated caller identity used to sign an outgoing Feign call when no
 * inbound HTTP request context is available (e.g. a scheduled batch job).
 *
 * @param user    the username to attribute the outgoing call to
 * @param role    the role associated with {@code user}
 * @param hotelId the hotel UUID the call is scoped to
 * @param branchId the branch UUID the call is scoped to, if present
 */
public record FeignAuthContext(String user, String role, String hotelId, String branchId) {
    /**
     * Creates an authenticated caller context without a branch scope.
     *
     * @param user the caller username
     * @param role the caller role
     * @param hotelId the tenant UUID
     */
    public FeignAuthContext(final String user, final String role, final String hotelId) {
        this(user, role, hotelId, null);
    }
}
