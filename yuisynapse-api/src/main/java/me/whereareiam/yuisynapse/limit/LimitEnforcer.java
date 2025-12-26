package me.whereareiam.yuisynapse.limit;

import reactor.core.publisher.Mono;

/**
 * Interface for enforcing rate limits and concurrency controls on AI requests.
 * Manages request throttling, token usage, and concurrent request limits.
 */
@SuppressWarnings("unused")
public interface LimitEnforcer {
    /**
     * Acquires permission to make a request, blocking if limit reached.
     * 
     * @return mono that completes when permission is granted
     */
    Mono<Void> acquireRequest();
    
    /**
     * Releases a previously acquired request slot.
     */
    void releaseRequest();
    
    /**
     * Attempts to acquire request permission without blocking.
     * 
     * @return true if permission granted, false if limit reached
     */
    boolean tryAcquireRequest();
    
    /**
     * Acquires permission to use a specific number of tokens.
     * 
     * @param tokenCount number of tokens to reserve
     * @return mono that completes when permission is granted
     */
    Mono<Void> acquireTokens(int tokenCount);
    
    /**
     * @return number of concurrent requests currently available
     */
    int getAvailableConcurrentRequests();
    
    /**
     * @return number of requests remaining in current rate limit window
     */
    int getAvailableRequestsPerMinute();
    
    /**
     * @return number of tokens remaining in current rate limit window
     */
    int getAvailableTokensPerMinute();
}
