package me.whereareiam.yuisynapse;

import me.whereareiam.yuisynapse.model.*;
import me.whereareiam.yuisynapse.session.SynapseSession;
import me.whereareiam.yuisynapse.tool.SynapseTool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Fluent builder for creating AI interactions.
 * Provides a chainable API for configuring and executing AI requests.
 */
@SuppressWarnings("unused")
public interface SynapseBuilder {
    /**
     * Configures this builder for a single message interaction.
     * 
     * @return this builder
     */
    SynapseBuilder message();
    
    /**
     * Configures this builder for a multi-turn conversation.
     * 
     * @return this builder
     */
    SynapseBuilder conversation();
    
    /**
     * Sets the system prompt that defines AI behavior.
     * 
     * @param prompt the system prompt
     * @return this builder
     */
    SynapseBuilder withSystemPrompt(String prompt);
    
    /**
     * Specifies which AI provider to use.
     * 
     * @param providerId the provider identifier
     * @return this builder
     */
    SynapseBuilder withProvider(String providerId);
    
    /**
     * Specifies which AI model to use.
     * 
     * @param model the model identifier
     * @return this builder
     */
    SynapseBuilder withModel(String model);
    
    /**
     * Sets the temperature parameter controlling response randomness.
     * 
     * @param temperature the temperature value (typically 0.0 to 2.0)
     * @return this builder
     */
    SynapseBuilder withTemperature(double temperature);
    
    /**
     * Sets the maximum number of tokens in the response.
     * 
     * @param maxTokens the maximum token count
     * @return this builder
     */
    SynapseBuilder withMaxTokens(int maxTokens);
    
    /**
     * Adds a user to the conversation.
     * 
     * @param user the user to add
     * @return this builder
     */
    SynapseBuilder withUser(me.whereareiam.yui.model.fluctlight.Fluctlight user);
    
    /**
     * Adds multiple users to the conversation.
     * 
     * @param users the users to add
     * @return this builder
     */
    SynapseBuilder withUsers(me.whereareiam.yui.model.fluctlight.Fluctlight... users);
    
    /**
     * Registers a named mode for later use in conversations.
     * 
     * @param name the mode name
     * @param mode the mode configuration
     * @return this builder
     */
    SynapseBuilder registerMode(String name, SynapseMode mode);
    
    /**
     * Activates a previously registered mode by name.
     * 
     * @param modeName the mode name
     * @return this builder
     */
    SynapseBuilder withMode(String modeName);
    
    /**
     * Activates a mode using direct configuration.
     * 
     * @param mode the mode configuration
     * @return this builder
     */
    SynapseBuilder withMode(SynapseMode mode);
    
    /**
     * Adds a tool to extend AI capabilities.
     * 
     * @param tool the tool to add
     * @return this builder
     */
    SynapseBuilder withTool(SynapseTool tool);
    
    /**
     * Adds multiple tools to extend AI capabilities.
     * 
     * @param tools the tools to add
     * @return this builder
     */
    SynapseBuilder withTools(SynapseTool... tools);
    
    /**
     * Enables streaming mode for progressive response delivery.
     * 
     * @return this builder
     */
    SynapseBuilder enableStreaming();
    
    /**
     * Sets a callback to receive streaming chunks as they arrive.
     * 
     * @param callback the stream chunk consumer
     * @return this builder
     */
    SynapseBuilder onStream(Consumer<StreamChunk> callback);
    
    /**
     * Sets a callback to receive the complete response.
     * 
     * @param callback the response consumer
     * @return this builder
     */
    SynapseBuilder onResponse(Consumer<SynapseResponse> callback);
    
    /**
     * Sets a callback to handle errors during AI interaction.
     * 
     * @param callback the error consumer
     * @return this builder
     */
    SynapseBuilder onError(Consumer<SynapseError> callback);
    
    /**
     * Sends a message and returns the AI response.
     * 
     * @param message the message to send
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> send(String message);
    
    /**
     * Sends a message from a specific user and returns the AI response.
     * 
     * @param message the message to send
     * @param fromUserId the user ID sending the message
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> send(String message, String fromUserId);
    
    /**
     * Starts a new conversation session.
     * 
     * @return future containing the created session
     */
    CompletableFuture<SynapseSession> start();
}
