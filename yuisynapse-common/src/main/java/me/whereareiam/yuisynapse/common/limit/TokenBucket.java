package me.whereareiam.yuisynapse.common.limit;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucket {
    private final int capacity;
    private final Duration refillPeriod;
    private final Lock lock = new ReentrantLock();
    
    private int availableTokens;
    private Instant lastRefillTime;
    
    public TokenBucket(int capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
        this.availableTokens = capacity;
        this.lastRefillTime = Instant.now();
    }
    
    public Mono<Void> consume(int tokens) {
        return Mono.defer(() -> {
            lock.lock();
            try {
                refill();
                
                if (availableTokens >= tokens) {
                    availableTokens -= tokens;
                    return Mono.empty();
                }
                
                long waitMs = calculateWaitTime(tokens);
                if (waitMs > 0) {
                    return Mono.delay(Duration.ofMillis(waitMs))
                            .then(consume(tokens));
                }
                
                return Mono.empty();
            } finally {
                lock.unlock();
            }
        });
    }
    
    public boolean tryConsume(int tokens) {
        lock.lock();
        try {
            refill();
            
            if (availableTokens >= tokens) {
                availableTokens -= tokens;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public int getAvailableTokens() {
        lock.lock();
        try {
            refill();
            return availableTokens;
        } finally {
            lock.unlock();
        }
    }
    
    private void refill() {
        Instant now = Instant.now();
        Duration elapsed = Duration.between(lastRefillTime, now);
        
        if (elapsed.compareTo(refillPeriod) >= 0) {
            availableTokens = capacity;
            lastRefillTime = now;
        }
    }
    
    private long calculateWaitTime(int tokens) {
        int deficit = tokens - availableTokens;
        if (deficit <= 0) return 0;
        
        double fractionOfCapacity = (double) deficit / capacity;
        return (long) (fractionOfCapacity * refillPeriod.toMillis());
    }
}
