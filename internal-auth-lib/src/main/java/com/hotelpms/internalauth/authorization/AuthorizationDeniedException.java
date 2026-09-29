package com.hotelpms.internalauth.authorization;

/** Deliberately non-descriptive authorization failure for safe API mapping. */
public final class AuthorizationDeniedException extends RuntimeException {
    public AuthorizationDeniedException(final String code) {
        super(code);
    }
}
