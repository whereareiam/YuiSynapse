package me.whereareiam.yuisynapse.provider.openai;

import me.whereareiam.yuisynapse.batching.BatchingCoordinator;
import me.whereareiam.yuisynapse.limit.LimitEnforcer;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.provider.openai.client.OpenAIHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenAIProviderFactory {
    private final ObjectProvider<SynapseSettings> settingsProvider;
    
    @Autowired
    public OpenAIProviderFactory(ObjectProvider<SynapseSettings> settingsProvider) {
        this.settingsProvider = settingsProvider;
    }
    
    public OpenAIProvider create(String providerId, LimitEnforcer limitEnforcer, BatchingCoordinator batchingCoordinator) {
        OpenAIHttpClient httpClient = new OpenAIHttpClient(settingsProvider, providerId, limitEnforcer);
        return new OpenAIProvider(httpClient, providerId, batchingCoordinator);
    }
}
