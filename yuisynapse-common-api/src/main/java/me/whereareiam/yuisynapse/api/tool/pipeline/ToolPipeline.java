package me.whereareiam.yuisynapse.api.tool.pipeline;

import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;

import java.util.List;

/**
 * Contract for running a tool pipeline prior to provider invocation.
 */
public interface ToolPipeline {
	ToolPipelineResult run(Connection connection, List<Message> input);
}


