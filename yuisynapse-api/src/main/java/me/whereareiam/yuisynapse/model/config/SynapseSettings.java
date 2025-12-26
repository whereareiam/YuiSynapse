package me.whereareiam.yuisynapse.model.config;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.annotation.Field;
import me.whereareiam.configura.annotation.MergeStrategy;
import me.whereareiam.yui.model.type.Duration;

import java.util.Map;

@Getter
@Setter
public class SynapseSettings {
    private String defaultProvider;
    @Field(merge = MergeStrategy.SHALLOW)
    private Map<String, ProviderConfig> providers;

    @Getter
    @Setter
    public static class ProviderConfig {
        private String url;
        private String key;
        private String schema;
        private String defaultModel;
        private Limit limit;
        private BatchingConfig batching;
    }
    
    @Getter
    @Setter
    public static class Limit {
        private RateLimit rate;
        private TokenLimit token;
    }
    
    @Getter
    @Setter
    public static class RateLimit {
        private int requestsPerMinute;
        private int maxConcurrentRequests;
    }
    
    @Getter
    @Setter
    public static class TokenLimit {
        private int tokensPerMinute;
        private int maxTokensPerRequest;
    }
    
    @Getter
    @Setter
    public static class BatchingConfig {
        private boolean enabled;
        private int maxBatchSize;
        private Duration batchWindow;
    }
}
