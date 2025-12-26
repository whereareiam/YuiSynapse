package me.whereareiam.yuisynapse.provider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yuisynapse.batching.BatchingCoordinator;
import me.whereareiam.yuisynapse.common.batching.DefaultBatchingCoordinator;
import me.whereareiam.yuisynapse.common.config.provider.SynapseSettingsProvider;
import me.whereareiam.yuisynapse.common.limit.DefaultLimitEnforcer;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import me.whereareiam.yuisynapse.limit.LimitEnforcer;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.provider.openai.OpenAIProviderFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderRegistrationService implements Reloadable {
    private final DefaultProviderRegistry providerRegistry;
    private final SynapseSettingsProvider settingsProvider;
    private final OpenAIProviderFactory openAiProviderFactory;
    private final Registry<Reloadable> reloadableRegistry;
    
    @PostConstruct
    public void initialize() {
        reloadableRegistry.register(this);
        registerProviders();
    }
    
    private void registerProviders() {
        SynapseSettings settings = settingsProvider.get();
        Map<String, SynapseSettings.ProviderConfig> providers = settings.getProviders();
        
        if (providers == null || providers.isEmpty()) {
            log.warn("No providers configured");
            return;
        }
        
        for (Map.Entry<String, SynapseSettings.ProviderConfig> entry : providers.entrySet()) {
            String providerId = entry.getKey();
            SynapseSettings.ProviderConfig config = entry.getValue();
            String schema = config.getSchema();
            
            if (schema == null) {
                log.warn("Provider {} has no schema configured, skipping", providerId);
                continue;
            }
            
            try {
                switch (schema.toUpperCase()) {
                    case "OPENAI" -> {
                        LimitEnforcer limitEnforcer = null;
                        if (config.getLimit() != null) {
                            limitEnforcer = new DefaultLimitEnforcer(providerId, config.getLimit());
                        }
                        
                        BatchingCoordinator batchingCoordinator = null;
                        if (config.getBatching() != null && config.getBatching().isEnabled()) {
                            var tempProvider = openAiProviderFactory.create(providerId, limitEnforcer, null);
                            batchingCoordinator = new DefaultBatchingCoordinator(
                                    config.getBatching().getMaxBatchSize(),
                                    config.getBatching().getBatchWindow(),
                                    tempProvider::processBatch
                            );
                            log.info("Batching enabled for provider: {} (window: {}, maxSize: {})", 
                                    providerId, 
                                    config.getBatching().getBatchWindow(),
                                    config.getBatching().getMaxBatchSize());
                        }
                        
                        var provider = openAiProviderFactory.create(providerId, limitEnforcer, batchingCoordinator);
                        providerRegistry.register(provider);
                        log.info("Registered provider: {} with schema: {}", providerId, schema);
                    }
                    default -> log.warn("Unknown schema: {} for provider: {}", schema, providerId);
                }
            } catch (Exception e) {
                log.error("Failed to register provider {}: {}", providerId, e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void reload() {
        log.info("Reloading provider registry");
        providerRegistry.clear();
        registerProviders();
        providerRegistry.reload();
    }
}