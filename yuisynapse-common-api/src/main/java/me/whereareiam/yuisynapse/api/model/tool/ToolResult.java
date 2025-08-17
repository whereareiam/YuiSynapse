package me.whereareiam.yuisynapse.api.model.tool;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import me.whereareiam.yuisynapse.api.model.Message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Outcome of a tool execution.
 */
@Getter
@Builder
public class ToolResult {
	/**
	 * Whether the input should be forwarded to the provider. If null, the tool does not decide.
	 */
	private final Boolean shouldForward;

	/**
	 * Optional interest score in [0,1].
	 */
	private final Double interestScore;

	/**
	 * Additional messages to prepend to the pipeline (e.g., system prompts, context blocks).
	 */
	@Singular("contextMessage")
	private final List<Message> contextMessages;

	/**
	 * Arbitrary metadata produced by the tool, merged into the run context for downstream tools.
	 */
	private final Map<String, Object> metadata;

	public List<Message> getContextMessages() {
		return contextMessages != null ? contextMessages : Collections.emptyList();
	}
}


