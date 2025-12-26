package me.whereareiam.yuisynapse.provider;

import me.whereareiam.yuisynapse.model.StreamChunk;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.request.SynapseSingleMessageRequest;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI provider implementations.
 * Providers handle communication with specific AI services (e.g., OpenAI, Anthropic).
 */
public interface SynapseProvider {
    /**
     * @return unique identifier for this provider
     */
    String getProviderId();
    
    /**
     * Sends a single message request to the AI provider.
     * 
     * @param request the single message request
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> sendSingleMessage(SynapseSingleMessageRequest request);
    
    /**
     * Sends a conversation request with full history to the AI provider.
     * 
     * @param request the conversation request
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> sendConversation(SynapseConversationRequest request);
    
    /**
     * Streams a conversation response progressively as chunks.
     * 
     * @param request the conversation request
     * @return reactive stream of response chunks
     */
    Flux<StreamChunk> streamConversation(SynapseConversationRequest request);
    
    /**
     * @return true if this provider supports streaming responses
     */
    boolean supportsStreaming();
}
