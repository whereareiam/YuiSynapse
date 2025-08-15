package me.whereareiam.yuisynapse.api.exception;

import me.whereareiam.yuisynapse.api.type.ProviderType;

/**
 * Exception thrown when a provider is not found.
 */
public class ProviderNotFoundException extends SynapseException {

	public ProviderNotFoundException(ProviderType providerType) {
		super("No client found for provider: " + providerType);
	}
}
