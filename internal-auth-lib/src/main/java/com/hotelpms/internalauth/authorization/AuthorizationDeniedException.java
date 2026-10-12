package com.hotelpms.internalauth.authorization;

/** Deliberately non-descriptive authorization failure for safe API mapping. */
public final class AuthorizationDeniedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an authorization failure with its internal mapping code.
     *
     * @param code internal mapping code.
     */
    public AuthorizationDeniedException(final String code) {
        super(code);
    }
}
