package me.whereareiam.yuisynapse.api.model.config;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yuisynapse.api.model.Provider;
import me.whereareiam.yuisynapse.api.type.ProviderType;

import java.util.Map;

@Setter
@Getter
public class SynapseSettings {
	private Map<ProviderType, Provider> providers;
}
