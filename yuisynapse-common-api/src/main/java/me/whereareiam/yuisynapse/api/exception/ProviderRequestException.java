package me.whereareiam.yuisynapse.api.exception;

/**
 * Exception thrown when a provider request fails.
 */
public class ProviderRequestException extends SynapseException {
    public ProviderRequestException(String message) {
        super(message);
    }

    public ProviderRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}


