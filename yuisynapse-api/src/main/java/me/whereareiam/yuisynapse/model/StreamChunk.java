package me.whereareiam.yuisynapse.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Represents a single chunk of data in a streaming AI response.
 * Used for progressive delivery of AI-generated content.
 */
@Data
@SuperBuilder
public class StreamChunk {
    private String content;
    private String delta;
    private boolean isComplete;
    private String sessionId;
    private int index;
}
