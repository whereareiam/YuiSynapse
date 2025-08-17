package me.whereareiam.yuisynapse.common.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolDefinition;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.pipeline.ToolPipeline;
import me.whereareiam.yuisynapse.api.tool.pipeline.ToolPipelineResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultToolPipeline implements ToolPipeline {
	private final ToolRegistry toolRegistry;

	@Override
	public ToolPipelineResult run(Connection connection, List<Message> input) {
		List<ToolDefinition> chain = Optional.ofNullable(connection.getConfiguration())
				.map(Connection.Configuration::getToolchain)
				.orElseGet(List::of);

		List<ToolDefinition> enabled = chain.stream()
				.filter(ToolDefinition::isEnabled)
				.sorted(Comparator.comparing(ToolDefinition::getPhase)
						.thenComparingInt(ToolDefinition::getOrder))
				.toList();

		List<Message> messages = new ArrayList<>(input);
		ToolRunContext runContext = ToolRunContext.create();
		Boolean shouldForward = null;
		Double interestScore = null;

		for (ToolDefinition definition : enabled) {
			Tool tool = toolRegistry.create(definition.getName());
			if (tool == null) {
				log.warn("Tool not found: {}", definition.getName());
				continue;
			}
			try {
				tool.configure(definition.getConfig());
				ToolResult result = tool.execute(connection, Collections.unmodifiableList(messages), runContext);
				if (result != null) {
					if (result.getContextMessages() != null && !result.getContextMessages().isEmpty()) {
						List<Message> newMessages = new ArrayList<>(result.getContextMessages());
						newMessages.addAll(messages);
						messages = newMessages;
					}
					if (result.getShouldForward() != null)
						shouldForward = result.getShouldForward();
					if (result.getInterestScore() != null)
						interestScore = result.getInterestScore();
					if (result.getMetadata() != null && !result.getMetadata().isEmpty())
						result.getMetadata().forEach(runContext::put);
				}
			} catch (Exception ex) {
				log.warn("Tool execution failed: {} - {}", definition.getName(), ex.toString());
			}
		}

		boolean finalForward = shouldForward != null ? shouldForward : true;

		return ToolPipelineResult.builder()
				.shouldForward(finalForward)
				.interestScore(interestScore)
				.messages(messages)
				.build();
	}
}


