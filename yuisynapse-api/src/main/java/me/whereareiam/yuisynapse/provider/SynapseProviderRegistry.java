package me.whereareiam.yuisynapse.provider;

import java.util.Collection;

/**
 * Registry for managing available AI providers.
 * Handles registration, retrieval, and default provider selection.
 */
@SuppressWarnings("unused")
public interface SynapseProviderRegistry {
    /**
     * Registers a new AI provider.
     * 
     * @param provider the provider to register
     */
    void register(SynapseProvider provider);
    
    /**
     * Retrieves a provider by its unique identifier.
     * 
     * @param id the provider identifier
     * @return the provider, or null if not found
     */
    SynapseProvider getProvider(String id);
    
    /**
     * @return the default provider to use when none is specified
     */
    SynapseProvider getDefaultProvider();
    
    /**
     * @return all registered providers
     */
    Collection<SynapseProvider> getAll();
}
