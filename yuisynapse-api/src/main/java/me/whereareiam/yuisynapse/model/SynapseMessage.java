package me.whereareiam.yuisynapse.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a message in a conversation with role, content, and metadata.
 * Used to build conversation history for AI interactions.
 */
@Data
@SuperBuilder
public class SynapseMessage {
    private String role;
    private String content;
    private String userId;
    private Instant timestamp;
    private Map<String, Object> metadata;
}
