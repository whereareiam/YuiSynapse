package me.whereareiam.yuisynapse.batching;

import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import reactor.core.publisher.Mono;

/**
 * Interface for coordinating batched request processing.
 * <p>
 * A batching coordinator collects multiple individual requests within a time window
 * and processes them together as a batch to improve throughput and reduce overhead.
 * Each submitted request receives its own response asynchronously.
 */
public interface BatchingCoordinator {
    /**
     * Submits a conversation request for batched processing.
     * <p>
     * The request will be collected and processed together with other requests
     * that arrive within the configured batch window. The returned Mono will
     * complete when the batch is processed and this specific request's response
     * is available.
     * 
     * @param request the conversation request to process
     * @return mono that completes with the response for this specific request
     */
    Mono<SynapseResponse> submitRequest(SynapseConversationRequest request);
}
