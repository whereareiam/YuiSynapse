package me.whereareiam.yuisynapse.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.common.config.template.SynapseSettingsTemplate;
import org.springframework.stereotype.Component;

@Component
public class SynapseSettingsProvider extends DefaultConfigProvider<SynapseSettings> {
    @Override
    protected SynapseSettings load() {
        return Config.update(getBasePath().resolve("settings"), SynapseSettings.class);
    }
    
    @Override
    protected void registerTemplate() {
        Config.registerTemplate(SynapseSettingsTemplate.class);
    }
    
    @Override
    public Class<SynapseSettings> getObjectType() {
        return SynapseSettings.class;
    }
}
