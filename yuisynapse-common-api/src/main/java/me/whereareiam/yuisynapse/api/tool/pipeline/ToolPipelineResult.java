package me.whereareiam.yuisynapse.api.tool.pipeline;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import me.whereareiam.yuisynapse.api.model.Message;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ToolPipelineResult {
	private final boolean shouldForward;
	private final Double interestScore;
	@Singular("message")
	private final List<Message> messages;

	public List<Message> getMessages() {
		return messages != null ? messages : Collections.emptyList();
	}
}


