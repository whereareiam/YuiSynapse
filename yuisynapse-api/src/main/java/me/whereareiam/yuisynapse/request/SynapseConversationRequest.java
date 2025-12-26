package me.whereareiam.yuisynapse.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.model.SynapseMessage;

import java.util.List;
import java.util.Map;

/**
 * Request object for multi-turn conversation interactions with AI.
 * Includes conversation history, multiple users, and configurable parameters.
 */
@Data
@SuperBuilder
public class SynapseConversationRequest {
    private String systemPrompt;
    private List<SynapseMessage> history;
    private List<Fluctlight> users;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Map<String, Object> parameters;
    private boolean stream;
}
