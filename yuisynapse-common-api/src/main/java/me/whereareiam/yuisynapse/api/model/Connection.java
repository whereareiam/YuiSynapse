package me.whereareiam.yuisynapse.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolDefinition;
import me.whereareiam.yuisynapse.api.type.ProviderType;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class Connection {
	private String id;
	private boolean enabled;

	private Configuration configuration;
	private Behavior behavior;

	@Getter
	@Setter
	@SuperBuilder
	@NoArgsConstructor
	public static class Configuration {
		private ProviderType provider;
		private String model;

		private List<ToolDefinition> toolchain;
	}

	@Getter
	@Setter
	@SuperBuilder
	@NoArgsConstructor
	public static class Behavior {
		private Map<String, List<String>> persona;
		private double temperature;
		private Limitations limitations;

		@Getter
		@Setter
		@SuperBuilder
		@NoArgsConstructor
		public static class Limitations {
			private int maxTokens;
		}
	}
}
