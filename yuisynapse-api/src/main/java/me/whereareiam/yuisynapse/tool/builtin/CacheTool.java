package me.whereareiam.yuisynapse.tool.builtin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CacheTool implements SynapseTool {
    private final String name;
    private final Map<String, CacheEntry> cache;
    private final Duration ttl;
    
    @Data
    private static class CacheEntry {
        private final String response;
        private final Instant timestamp;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void beforeMessage(ToolContext context) {
        String key = context.getMessage().toLowerCase().trim();
        CacheEntry entry = cache.get(key);
        
        if (entry != null && Duration.between(entry.timestamp, Instant.now()).compareTo(ttl) < 0) {
            context.getSessionData().put("cached_response", entry.response);
            context.getSessionData().put("skip_ai", true);
        }
    }
    
    @Override
    public void afterMessage(ToolContext context, SynapseResponse response) {
        String key = context.getMessage().toLowerCase().trim();
        cache.put(key, new CacheEntry(response.getContent(), Instant.now()));
    }
    
    public CacheTool(String name, Duration ttl) {
        this.name = name;
        this.cache = new ConcurrentHashMap<>();
        this.ttl = ttl;
    }
}
