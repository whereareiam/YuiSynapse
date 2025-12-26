package me.whereareiam.yuisynapse.common.limit;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.limit.LimitEnforcer;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultLimitEnforcer implements LimitEnforcer {
    private final AtomicInteger concurrentRequests;
    private final int maxConcurrent;
    private final TokenBucket requestsPerMinuteBucket;
    private final TokenBucket tokensPerMinuteBucket;
    
    public DefaultLimitEnforcer(String providerId, SynapseSettings.Limit config) {
        this.maxConcurrent = config.getRate() != null ? config.getRate().getMaxConcurrentRequests() : 10;
        int requestsPerMin = config.getRate() != null ? config.getRate().getRequestsPerMinute() : 60;
        int tokensPerMin = config.getToken() != null ? config.getToken().getTokensPerMinute() : 90000;
        
        this.concurrentRequests = new AtomicInteger(0);
        this.requestsPerMinuteBucket = new TokenBucket(requestsPerMin, Duration.ofMinutes(1));
        this.tokensPerMinuteBucket = new TokenBucket(tokensPerMin, Duration.ofMinutes(1));
        
        log.info("Initialized limit enforcer for provider {}: {} req/min, {} tokens/min, {} concurrent",
            providerId, requestsPerMin, tokensPerMin, maxConcurrent);
    }
    
    @Override
    public Mono<Void> acquireRequest() {
        return Mono.defer(() -> {
            if (concurrentRequests.get() >= maxConcurrent) {
                return Mono.delay(Duration.ofMillis(100))
                        .then(acquireRequest());
            }
            
            return requestsPerMinuteBucket.consume(1)
                    .doOnSuccess(_ -> concurrentRequests.incrementAndGet());
        });
    }
    
    @Override
    public void releaseRequest() {
        concurrentRequests.decrementAndGet();
    }
    
    @Override
    public Mono<Void> acquireTokens(int tokenCount) {
        return tokensPerMinuteBucket.consume(tokenCount);
    }
    
    @Override
    public boolean tryAcquireRequest() {
        if (concurrentRequests.get() >= maxConcurrent) {
            return false;
        }
        
        if (requestsPerMinuteBucket.tryConsume(1)) {
            concurrentRequests.incrementAndGet();
            return true;
        }
        return false;
    }
    
    @Override
    public int getAvailableConcurrentRequests() {
        return Math.max(0, maxConcurrent - concurrentRequests.get());
    }
    
    @Override
    public int getAvailableRequestsPerMinute() {
        return requestsPerMinuteBucket.getAvailableTokens();
    }
    
    @Override
    public int getAvailableTokensPerMinute() {
        return tokensPerMinuteBucket.getAvailableTokens();
    }
}
