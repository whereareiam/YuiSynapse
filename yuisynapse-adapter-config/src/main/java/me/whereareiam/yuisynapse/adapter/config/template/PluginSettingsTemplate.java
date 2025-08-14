package me.whereareiam.yuisynapse.adapter.config.template;

import me.whereareiam.yui.api.output.config.DefaultConfig;
import me.whereareiam.yuisynapse.api.model.config.YuiSynapseSettings;
import org.springframework.stereotype.Component;

@Component
public class PluginSettingsTemplate implements DefaultConfig<YuiSynapseSettings> {
	@Override
	public YuiSynapseSettings getDefault() {
		YuiSynapseSettings settings = new YuiSynapseSettings();

		// Default values

		return settings;
	}
}
