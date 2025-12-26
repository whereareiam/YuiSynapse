package me.whereareiam.yuisynapse.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SynapseSettingsTemplate implements TemplateProvider<SynapseSettings> {
    @Override
    public SynapseSettings supply(SynapseSettings settings) {
        settings.setDefaultProvider("openai-primary");
        
        Map<String, SynapseSettings.ProviderConfig> providers = new HashMap<>();
        
        // OpenAI Primary
        SynapseSettings.ProviderConfig primaryConfig = new SynapseSettings.ProviderConfig();
        primaryConfig.setKey("${OPENAI_API_KEY}");
        primaryConfig.setUrl("https://api.openai.com/v1");
        primaryConfig.setSchema("OPENAI");
        primaryConfig.setDefaultModel("gpt-4");
        
        SynapseSettings.Limit primaryLimit = new SynapseSettings.Limit();
        SynapseSettings.RateLimit primaryRateLimit = new SynapseSettings.RateLimit();
        primaryRateLimit.setRequestsPerMinute(60);
        primaryRateLimit.setMaxConcurrentRequests(10);
        primaryLimit.setRate(primaryRateLimit);
        
        SynapseSettings.TokenLimit primaryTokenLimit = new SynapseSettings.TokenLimit();
        primaryTokenLimit.setTokensPerMinute(90000);
        primaryTokenLimit.setMaxTokensPerRequest(4000);
        primaryLimit.setToken(primaryTokenLimit);
        
        primaryConfig.setLimit(primaryLimit);
        
        SynapseSettings.BatchingConfig primaryBatching = new SynapseSettings.BatchingConfig();
        primaryBatching.setEnabled(false);
        primaryBatching.setMaxBatchSize(10);
        primaryBatching.setBatchWindow(me.whereareiam.yui.model.type.Duration.parse("100ms"));
        primaryConfig.setBatching(primaryBatching);
        
        providers.put("openai-primary", primaryConfig);
        
        // OpenAI Fast
        SynapseSettings.ProviderConfig fastConfig = new SynapseSettings.ProviderConfig();
        fastConfig.setKey("${OPENAI_API_KEY}");
        fastConfig.setUrl("https://api.openai.com/v1");
        fastConfig.setSchema("OPENAI");
        fastConfig.setDefaultModel("gpt-3.5-turbo");
        
        SynapseSettings.Limit fastLimit = new SynapseSettings.Limit();
        SynapseSettings.RateLimit fastRateLimit = new SynapseSettings.RateLimit();
        fastRateLimit.setRequestsPerMinute(100);
        fastRateLimit.setMaxConcurrentRequests(20);
        fastLimit.setRate(fastRateLimit);
        
        SynapseSettings.TokenLimit fastTokenLimit = new SynapseSettings.TokenLimit();
        fastTokenLimit.setTokensPerMinute(150000);
        fastTokenLimit.setMaxTokensPerRequest(2000);
        fastLimit.setToken(fastTokenLimit);
        
        fastConfig.setLimit(fastLimit);
        
        SynapseSettings.BatchingConfig fastBatching = new SynapseSettings.BatchingConfig();
        fastBatching.setEnabled(false);
        fastBatching.setMaxBatchSize(15);
        fastBatching.setBatchWindow(me.whereareiam.yui.model.type.Duration.parse("50ms"));
        fastConfig.setBatching(fastBatching);
        
        providers.put("openai-fast", fastConfig);
        
        settings.setProviders(providers);
        
        return settings;
    }
}
