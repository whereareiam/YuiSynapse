package me.whereareiam.yuisynapse.api.model.config.tool;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoToolConfig extends ToolConfig {
	private boolean includePrimaryLanguage = true;
	private boolean includeAdditionalLanguages = true;
	private boolean includeDiscordTag = true;
	private boolean includeDisplayName = true;
}
