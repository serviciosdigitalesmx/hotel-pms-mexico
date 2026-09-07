package com.hotelpms.frontdesk.assistant;

/** Provider rejection that must not be retried. */
public final class PermanentAiProviderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a non-retryable provider failure.
     *
     * @param message provider failure description
     */
    public PermanentAiProviderException(final String message) {
        super(message);
    }
}
