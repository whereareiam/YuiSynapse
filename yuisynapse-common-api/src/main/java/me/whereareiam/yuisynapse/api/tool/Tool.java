package me.whereareiam.yuisynapse.api.tool;

import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.type.ToolPhase;

import java.util.List;

/**
 * Core Tool abstraction. Implementations may mutate messages, add context, or produce control outputs.
 */
public interface Tool {
	String name();

	ToolPhase phase();

	/**
	 * Configure the tool for a given connection using the provided configuration object.
	 * The config object is expected to be a simple POJO representing tool-specific settings.
	 */
	void configure(ToolConfig config);

	/**
	 * Execute the tool on the given connection and input messages. Tools may emit a {@link ToolResult}
	 * that can add context messages, modify routing, or annotate metadata.
	 */
	ToolResult execute(Connection connection, List<Message> input, ToolRunContext runContext);
}


