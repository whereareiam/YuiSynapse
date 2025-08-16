package me.whereareiam.yuisynapse.adapter.config.template;

import me.whereareiam.yui.api.output.config.DefaultConfig;
import me.whereareiam.yuisynapse.api.model.Provider;
import me.whereareiam.yuisynapse.api.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SynapseSettingsTemplate implements DefaultConfig<SynapseSettings> {
	@Override
	public SynapseSettings getDefault() {
		SynapseSettings settings = new SynapseSettings();

		// Default values
		settings.setProviders(Map.of(
				ProviderType.OPENROUTER, Provider.builder()
						.keys(List.of("YOUR_OPENROUTER_API_KEY"))
						.url("https://openrouter.ai/api/v1")
						.rateLimitCooldown(300)
						.build()
		));

		return settings;
	}
}
