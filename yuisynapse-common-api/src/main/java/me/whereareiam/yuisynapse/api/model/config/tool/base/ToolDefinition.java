package me.whereareiam.yuisynapse.api.model.config.tool.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yuisynapse.api.type.ToolPhase;

/**
 * A declarative definition of a tool instance bound to a connection, including its configuration and execution order.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class ToolDefinition {
	private String name;
	private ToolPhase phase;
	private int order;
	private boolean enabled;
	private ToolConfig config;
}


