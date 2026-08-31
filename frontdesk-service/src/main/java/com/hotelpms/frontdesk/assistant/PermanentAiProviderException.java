package com.hotelpms.frontdesk.assistant;

/** Provider rejection that must not be retried. */
public final class PermanentAiProviderException extends RuntimeException {
    public PermanentAiProviderException(final String message) {
        super(message);
    }
}
