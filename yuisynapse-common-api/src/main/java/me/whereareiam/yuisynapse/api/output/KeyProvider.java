package me.whereareiam.yuisynapse.api.output;

import me.whereareiam.yuisynapse.api.type.ProviderType;

/**
 * Port used by provider adapters to acquire and report usage of API keys.
 * Implemented in the common/application layer.
 */
public interface KeyProvider {
    /**
     * Acquire a usable key for the given provider type considering configured rate
     * limits and temporary blocks. Returns null when no key is currently available.
     * Implementations should also record the usage to enforce rate limits.
     */
    String acquire(ProviderType providerType);

    /**
     * Report a successful usage of the key. Optional for implementations that
     * already account on acquire.
     */
    void onSuccess(ProviderType providerType, String key);

    /**
     * Report that the key was rate-limited. Implementation should block the key
     * for a cooldown period to avoid immediate reuse.
     */
    void onRateLimited(ProviderType providerType, String key);

    /**
     * Report that the key failed due to transient errors (e.g., network/5xx).
     * Implementation should temporarily avoid the key.
     */
    void onFailure(ProviderType providerType, String key);

    /**
     * The number of configured keys for this provider. Used to cap failover attempts.
     */
    int keyCapacity(ProviderType providerType);
}


