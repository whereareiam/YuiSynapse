package me.whereareiam.yuisynapse.tools.history;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.api.MetaKeys;
import me.whereareiam.yuisynapse.api.input.HistoryStore;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.HistoryToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.type.ToolPhase;
import me.whereareiam.yuisynapse.tools.ToolMetadataKeys;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HistoryTool implements Tool {
	private final HistoryStore historyStore;
	private HistoryToolConfig toolConfig;

	@Override
	public String name() {
		return "HISTORY";
	}

	@Override
	public ToolPhase phase() {
		return ToolPhase.CONTEXT_PROVIDER;
	}

	@Override
	public void configure(ToolConfig config) {
		if (!(config instanceof HistoryToolConfig c)) return;

		this.toolConfig = c;
	}

	@Override
	public ToolResult execute(Connection connection, List<Message> input, ToolRunContext runContext) {
		if (historyStore != null) {
			List<Message> messages = input.stream()
					.filter(m -> m.getRole() == Message.Role.USER || m.getRole() == Message.Role.ASSISTANT)
					.toList();
			if (!messages.isEmpty())
				historyStore.append(
						String.valueOf(toolConfig.isPerUser()),
						String.valueOf(toolConfig.isPerChannel()),
						messages
				);
		}

		List<Message> history = new ArrayList<>();
		if (historyStore != null) {
			history.addAll(historyStore.read(
					String.valueOf(toolConfig.isPerUser()),
					String.valueOf(toolConfig.isPerChannel()),
					toolConfig.getRetentionCount(),
					Duration.ofSeconds(toolConfig.getTtlSeconds())
			));

			Set<String> inputIds = input.stream()
					.map(Message::getId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			history.removeIf(m -> (m.getId() != null && inputIds.contains(m.getId())));
		}

		if (history.isEmpty()) {
			String recentAssistant = extractRecentAssistantText(input);
			if (recentAssistant != null && !recentAssistant.isBlank()) {
				Message sysRecent = Message.builder().role(Message.Role.SYSTEM)
						.content("Recent AI message:\n" + recentAssistant)
						.metadata(Map.of(ToolMetadataKeys.TOOL, "history"))
						.build();
				return ToolResult.builder().contextMessage(sysRecent).build();
			}

			return ToolResult.builder().build();
		}

		List<Message> context = new ArrayList<>(history);
		return ToolResult.builder()
				.contextMessages(context)
				.build();
	}

	private String extractRecentAssistantText(List<Message> input) {
		return input.stream()
				.filter(m -> m.getMetadata() != null && m.getMetadata().get(MetaKeys.RECENT_ASSISTANT_TEXT) != null)
				.map(m -> String.valueOf(m.getMetadata().get(MetaKeys.RECENT_ASSISTANT_TEXT)))
				.findFirst().orElse(null);
	}
}