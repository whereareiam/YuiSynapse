package me.whereareiam.yuisynapse.common.batching;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.batching.BatchingCoordinator;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class DefaultBatchingCoordinator implements BatchingCoordinator {
    private final int maxBatchSize;
    private final Duration batchWindow;
    private final ConcurrentLinkedQueue<BatchedRequest> pendingRequests;
    private final Function<List<SynapseConversationRequest>, Mono<List<SynapseResponse>>> batchProcessor;
    
    private volatile boolean processingScheduled = false;
    
    public DefaultBatchingCoordinator(
            int maxBatchSize,
            me.whereareiam.yui.model.type.Duration batchWindow,
            Function<List<SynapseConversationRequest>, Mono<List<SynapseResponse>>> batchProcessor
    ) {
        this.maxBatchSize = maxBatchSize;
        this.batchWindow = Duration.ofMillis(batchWindow.to(TimeUnit.MILLISECONDS));
        this.batchProcessor = batchProcessor;
        this.pendingRequests = new ConcurrentLinkedQueue<>();
    }
    
    @Override
    public Mono<SynapseResponse> submitRequest(SynapseConversationRequest request) {
        Sinks.One<SynapseResponse> sink = Sinks.one();
        BatchedRequest batchedRequest = new BatchedRequest(request, sink);
        
        pendingRequests.offer(batchedRequest);
        scheduleProcessing();
        
        return sink.asMono();
    }
    
    private void scheduleProcessing() {
        if (processingScheduled)
            return;
        
        synchronized (this) {
            if (processingScheduled)
                return;

            processingScheduled = true;
        }
        
        Mono.delay(batchWindow)
                .then(Mono.defer(this::processBatch))
                .doFinally(_ -> processingScheduled = false)
                .subscribe(
		                _ -> {},
                        error -> log.error("Error processing batch", error)
                );
    }
    
    private Mono<Void> processBatch() {
        List<BatchedRequest> batch = new ArrayList<>();
        
        while (!pendingRequests.isEmpty() && batch.size() < maxBatchSize) {
            BatchedRequest request = pendingRequests.poll();
            if (request != null) {
                batch.add(request);
            }
        }
        
        if (batch.isEmpty()) {
            return Mono.empty();
        }
        
        log.debug("Processing batch of {} requests", batch.size());
        
        List<SynapseConversationRequest> requests = batch.stream()
                .map(BatchedRequest::request)
                .toList();
        
        return batchProcessor.apply(requests)
                .doOnSuccess(responses -> {
                    for (int i = 0; i < batch.size(); i++) {
                        if (i < responses.size()) {
                            batch.get(i).sink().tryEmitValue(responses.get(i));
                            continue;
                        }

                        batch.get(i).sink().tryEmitError(
                                new RuntimeException("Batch response missing for request " + i));
                    }
                })
                .doOnError(error -> {
                    for (BatchedRequest req : batch)
                        req.sink().tryEmitError(error);
                })
                .then();
    }
    
    private record BatchedRequest(
            SynapseConversationRequest request,
            Sinks.One<SynapseResponse> sink
    ) {}
}
