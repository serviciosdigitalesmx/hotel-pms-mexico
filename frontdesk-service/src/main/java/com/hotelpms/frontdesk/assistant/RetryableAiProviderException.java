package com.hotelpms.frontdesk.assistant;

/** Provider failure that is safe to retry. */
public final class RetryableAiProviderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a retryable provider failure.
     *
     * @param message provider failure description
     */
    public RetryableAiProviderException(final String message) {
        super(message);
    }

    /**
     * Creates a retryable failure retaining its original cause.
     *
     * @param message provider failure description
     * @param cause underlying failure
     */
    public RetryableAiProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
