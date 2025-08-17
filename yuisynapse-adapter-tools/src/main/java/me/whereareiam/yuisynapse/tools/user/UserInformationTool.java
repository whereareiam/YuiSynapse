package me.whereareiam.yuisynapse.tools.user;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.util.Users;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.UserInfoToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.type.ToolPhase;
import me.whereareiam.yuisynapse.tools.ToolMetadataKeys;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserInformationTool implements Tool {
	private UserInfoToolConfig toolConfig;

	@Override
	public String name() {
		return "USER_INFO";
	}

	@Override
	public ToolPhase phase() {
		return ToolPhase.CONTEXT_PROVIDER;
	}

	@Override
	public void configure(ToolConfig config) {
		if (!(config instanceof UserInfoToolConfig c)) return;
		this.toolConfig = c;
	}

	@Override
	public ToolResult execute(Connection connection, List<Message> input, ToolRunContext runContext) {
		if (input == null || input.isEmpty())
			return ToolResult.builder().build();

		Message user = input.stream().filter(m -> m.getRole() == Message.Role.USER).findFirst().orElse(null);
		if (user == null)
			return ToolResult.builder().build();

		Long authorId = extractAuthorId(user);
		if (authorId == null)
			return ToolResult.builder().build();

		boolean includePrimary = toolConfig.isIncludePrimaryLanguage();
		boolean includeAdditional = toolConfig.isIncludeAdditionalLanguages();
		boolean includeDiscordTag = toolConfig.isIncludeDiscordTag();
		boolean includeDisplayName = toolConfig.isIncludeDisplayName();

		Map<String, Object> ctx = new LinkedHashMap<>();

		if (includePrimary || includeAdditional) {
			Optional<UserProfile> profileOpt = Users.get(authorId);
			profileOpt.ifPresent(profile -> {
				if (includePrimary && profile.getPrimaryLanguage() != null)
					ctx.put(ToolMetadataKeys.PRIMARY_LANGUAGE, profile.getPrimaryLanguage().getLocale());

				if (includeAdditional && profile.getAdditionalLanguages() != null) {
					List<String> langs = new ArrayList<>();
					for (DiscordLocale locale : profile.getAdditionalLanguages()) {
						if (locale != null)
							langs.add(locale.getLocale());
					}

					ctx.put(ToolMetadataKeys.ADDITIONAL_LANGUAGES, langs);
				}
			});
		}

		if (includeDiscordTag)
			ctx.put(ToolMetadataKeys.DISCORD_TAG, Users.getMention(authorId));
		if (includeDisplayName)
			ctx.put(ToolMetadataKeys.DISPLAY_NAME, Users.getUsername(authorId));

		if (ctx.isEmpty()) return ToolResult.builder().build();

		Message sys = Message.builder()
				.role(Message.Role.SYSTEM)
				.content(render(ctx))
				.metadata(Map.of(ToolMetadataKeys.TOOL, name()))
				.build();

		return ToolResult.builder()
				.contextMessage(sys)
				.metadata(ctx)
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

	private String render(Map<String, Object> ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("User context:\n");
		for (var e : ctx.entrySet())
			sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
		return sb.toString();
	}
}
