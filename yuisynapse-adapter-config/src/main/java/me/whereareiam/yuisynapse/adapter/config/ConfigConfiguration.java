package me.whereareiam.yuisynapse.adapter.config;

import me.whereareiam.yui.api.output.config.ConfigurationManager;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import me.whereareiam.yuisynapse.adapter.config.provider.SynapseSettingsProvider;
import me.whereareiam.yuisynapse.api.model.config.SynapseSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigConfiguration {
	@Bean
	public SynapseSettings settings(SynapseSettingsProvider synapseSettingsProvider) {
		return synapseSettingsProvider.get();
	}

	@Autowired
	public void setTemplates(ApplicationContext ctx, ConfigurationManager configManager) {
		configManager.addTemplate(SynapseSettings.class, ctx.getBean("synapseSettingsTemplate", DefaultConfig.class));
	}
}
