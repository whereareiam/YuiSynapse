package me.whereareiam.yuisynapse.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Request object for single-message AI interactions without conversation context.
 * Suitable for stateless, one-off queries to AI models.
 */
@Data
@SuperBuilder
public class SynapseSingleMessageRequest {
	private String systemPrompt;
	private String message;
	private String model;
	private Double temperature;
	private Integer maxTokens;
	private Map<String, Object> parameters;
}
