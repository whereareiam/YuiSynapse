package me.whereareiam.yuisynapse.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Defines a conversation mode that modifies AI behavior through prompt and parameter adjustments.
 * Allows switching between different AI interaction styles within a session.
 */
@Data
@SuperBuilder
public class SynapseMode {
    private String name;
    private String systemPromptModifier;
    private Double temperatureModifier;
    private Integer maxTokensModifier;
    private Map<String, Object> parameters;
}
