package me.whereareiam.yuisynapse.tool;

import me.whereareiam.yuisynapse.model.SynapseResponse;

/**
 * Interface for tools that extend AI session capabilities.
 * Tools can hook into session lifecycle events and provide context data.
 */
public interface SynapseTool {
    /**
     * @return unique name of this tool
     */
    String getName();
    
    /**
     * Called when a new session starts.
     * 
     * @param context the tool context
     */
    default void onSessionStart(ToolContext context) {}
    
    /**
     * Called before each message is sent to the AI.
     * 
     * @param context the tool context
     */
    default void beforeMessage(ToolContext context) {}
    
    /**
     * Called after receiving a response from the AI.
     * 
     * @param context the tool context
     * @param response the AI response
     */
    default void afterMessage(ToolContext context, SynapseResponse response) {}
    
    /**
     * Provides contextual data to be included with AI requests.
     * 
     * @param context the tool context
     * @return context data string, or null if none
     */
    default String getContextData(ToolContext context) {
        return null;
    }
    
    /**
     * @return priority for tool execution order (lower = higher priority)
     */
    default int getPriority() {
        return 100;
    }
}
