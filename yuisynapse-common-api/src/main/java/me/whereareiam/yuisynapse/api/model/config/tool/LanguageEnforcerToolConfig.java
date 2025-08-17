package me.whereareiam.yuisynapse.api.model.config.tool;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;

@Getter
@Setter
@NoArgsConstructor
public class LanguageEnforcerToolConfig extends ToolConfig {
	public enum EnforcementMode {STRICT, SOFT}

	private EnforcementMode mode;
}


