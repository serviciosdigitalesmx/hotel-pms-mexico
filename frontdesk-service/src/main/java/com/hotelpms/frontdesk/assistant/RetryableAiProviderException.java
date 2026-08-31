package com.hotelpms.frontdesk.assistant;

/** Provider failure that is safe to retry. */
public final class RetryableAiProviderException extends RuntimeException {
    public RetryableAiProviderException(final String message) {
        super(message);
    }

    public RetryableAiProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
