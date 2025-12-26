package me.whereareiam.yuisynapse.common.provider;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yuisynapse.common.config.provider.SynapseSettingsProvider;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.provider.SynapseProvider;
import me.whereareiam.yuisynapse.provider.SynapseProviderRegistry;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DefaultProviderRegistry implements SynapseProviderRegistry, Reloadable {
    private final Map<String, SynapseProvider> providers = new ConcurrentHashMap<>();
    private final SynapseSettingsProvider settingsProvider;
    
    @Override
    public void register(SynapseProvider provider) {
        providers.put(provider.getProviderId(), provider);
    }
    
    public void clear() {
        providers.clear();
    }
    
    @Override
    public SynapseProvider getProvider(String id) {
        return providers.get(id);
    }
    
    @Override
    public SynapseProvider getDefaultProvider() {
        SynapseSettings settings = settingsProvider.get();
        String defaultId = settings.getDefaultProvider();
        return providers.get(defaultId);
    }
    
    @Override
    public Collection<SynapseProvider> getAll() {
        return providers.values();
    }
    
    @Override
    public void reload() {
        for (SynapseProvider provider : providers.values()) {
            if (provider instanceof Reloadable reloadable) {
                reloadable.reload();
            }
        }
    }
}
