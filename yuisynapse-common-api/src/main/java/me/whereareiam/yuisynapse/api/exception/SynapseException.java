package me.whereareiam.yuisynapse.api.exception;

/**
 * Base exception for all Synapse-related errors.
 */
public class SynapseException extends RuntimeException {
	public SynapseException(String message) {
		super(message);
	}

	public SynapseException(String message, Throwable cause) {
		super(message, cause);
	}
}
