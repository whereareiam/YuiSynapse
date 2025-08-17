package me.whereareiam.yuisynapse.tools.language;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.util.Users;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.LanguageEnforcerToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.type.ToolPhase;
import me.whereareiam.yuisynapse.tools.ToolMetadataKeys;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LanguageEnforcerTool implements Tool {
	private LanguageEnforcerToolConfig toolConfig;

	@Override
	public String name() {
		return "LANGUAGE_ENFORCER";
	}

	@Override
	public ToolPhase phase() {
		return ToolPhase.PRE_PROCESSING;
	}

	@Override
	public void configure(ToolConfig config) {
		if (!(config instanceof LanguageEnforcerToolConfig c)) return;

		this.toolConfig = c;
	}

	@Override
	public ToolResult execute(Connection connection, List<Message> input, ToolRunContext runContext) {
		if (input == null || input.isEmpty())
			return ToolResult.builder().build();

		Message firstUser = input.stream().filter(m -> m.getRole() == Message.Role.USER).findFirst().orElse(null);
		if (firstUser == null)
			return ToolResult.builder().build();

		Long authorId = extractAuthorId(firstUser);
		if (authorId == null)
			return ToolResult.builder().build();

		Optional<UserProfile> profileOpt = Users.get(authorId);
		if (profileOpt.isEmpty())
			return ToolResult.builder().build();

		UserProfile profile = profileOpt.get();
		String primary = profile.getPrimaryLanguage().getLocale();
		List<String> additional = new ArrayList<>();
		if (profile.getAdditionalLanguages() != null)
			for (DiscordLocale locale : profile.getAdditionalLanguages())
				additional.add(locale.getLocale());

		String systemPrompt = buildPrompt(primary, additional);
		if (systemPrompt == null)
			return ToolResult.builder().build();

		Message sys = Message.builder().role(Message.Role.SYSTEM).content(systemPrompt).metadata(Map.of(ToolMetadataKeys.TOOL, name())).build();
		return ToolResult.builder()
				.contextMessage(sys)
				.build();
	}

	private Long extractAuthorId(Message message) {
		if (message.getAuthor() == null) return null;
		try {
			return Long.parseLong(message.getAuthor().getId());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String buildPrompt(String primary, List<String> additional) {
		if (primary == null) return null;

		StringBuilder sb = new StringBuilder();
		sb.append("The user prefers replies in ").append(primary).append(".");
		if (toolConfig.getMode() == LanguageEnforcerToolConfig.EnforcementMode.STRICT) {
			sb.append(" Always respond in this language unless explicitly asked otherwise.");
		} else {
			sb.append(" Prefer this language; you may switch if the user switches.");
		}

		if (!additional.isEmpty())
			sb.append(" Additional acceptable languages: ").append(String.join(", ", additional)).append(".");

		return sb.toString();
	}
}
