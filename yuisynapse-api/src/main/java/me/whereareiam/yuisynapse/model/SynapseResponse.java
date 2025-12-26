package me.whereareiam.yuisynapse.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete response from an AI provider.
 * Contains the generated content, usage statistics, and metadata.
 */
@Data
@SuperBuilder
public class SynapseResponse {
    private String content;
    private String sessionId;
    private int tokensUsed;
    private Duration processingTime;
    private String model;
    private boolean isStreaming;
    private List<ToolCall> toolCalls;
    private Map<String, Object> providerMetadata;
    
    /**
     * Represents a tool invocation made by the AI during response generation.
     */
    @Data
    @SuperBuilder
    public static class ToolCall {
        private String toolId;
        private String toolName;
        private Map<String, Object> arguments;
        private String result;
    }
}
