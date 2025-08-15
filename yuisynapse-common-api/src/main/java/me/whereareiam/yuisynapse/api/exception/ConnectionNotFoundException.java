package me.whereareiam.yuisynapse.api.exception;

/**
 * Exception thrown when a connection is not found.
 */
public class ConnectionNotFoundException extends SynapseException {
	public ConnectionNotFoundException(String connectionId) {
		super("Connection not found: " + connectionId);
	}
}
