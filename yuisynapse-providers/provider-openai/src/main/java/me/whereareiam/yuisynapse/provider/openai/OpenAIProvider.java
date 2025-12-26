package me.whereareiam.yuisynapse.provider.openai;

import me.whereareiam.yui.Reloadable;
import me.whereareiam.yuisynapse.batching.BatchingCoordinator;
import me.whereareiam.yuisynapse.model.StreamChunk;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.provider.SynapseProvider;
import me.whereareiam.yuisynapse.provider.openai.client.OpenAIHttpClient;
import me.whereareiam.yuisynapse.provider.openai.mapper.RequestMapper;
import me.whereareiam.yuisynapse.provider.openai.mapper.ResponseMapper;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIRequest;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.request.SynapseSingleMessageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenAIProvider implements SynapseProvider, Reloadable {
    private final OpenAIHttpClient httpClient;
    private final String providerId;
    private final BatchingCoordinator batchingCoordinator;
    
    public OpenAIProvider(OpenAIHttpClient httpClient, String providerId, BatchingCoordinator batchingCoordinator) {
        this.httpClient = httpClient;
        this.providerId = providerId;
        this.batchingCoordinator = batchingCoordinator;
    }
    
    @Override
    public String getProviderId() {
        return providerId;
    }
    
    @Override
    public void reload() {
        httpClient.reload();
    }
    
    @Override
    public CompletableFuture<SynapseResponse> sendSingleMessage(SynapseSingleMessageRequest request) {
        Instant start = Instant.now();
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(request);
        
        return httpClient.sendRequest(openAiRequest)
            .map(openAiResponse -> {
                SynapseResponse response = ResponseMapper.toSynapse(openAiResponse);
                response.setProcessingTime(Duration.between(start, Instant.now()));
                return response;
            })
            .toFuture();
    }
    
    @Override
    public CompletableFuture<SynapseResponse> sendConversation(SynapseConversationRequest request) {
        if (batchingCoordinator != null) {
            return batchingCoordinator.submitRequest(request).toFuture();
        }
        
        return sendConversationDirect(request).toFuture();
    }
    
    private Mono<SynapseResponse> sendConversationDirect(SynapseConversationRequest request) {
        Instant start = Instant.now();
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(request);
        
        return httpClient.sendRequest(openAiRequest)
            .map(openAiResponse -> {
                SynapseResponse response = ResponseMapper.toSynapse(openAiResponse);
                response.setProcessingTime(Duration.between(start, Instant.now()));
                return response;
            });
    }
    
    public Mono<List<SynapseResponse>> processBatch(List<SynapseConversationRequest> requests) {
        return Flux.fromIterable(requests)
                .flatMap(this::sendConversationDirect)
                .collectList();
    }
    
    @Override
    public Flux<StreamChunk> streamConversation(SynapseConversationRequest request) {
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(request);
        
        return httpClient.streamRequest(openAiRequest)
            .flatMap(streamResponse -> {
                if (streamResponse.getChoices() == null || streamResponse.getChoices().isEmpty()) {
                    return Flux.empty();
                }
                
                var choice = streamResponse.getChoices().getFirst();
                var delta = choice.getDelta();
                
                if (delta == null || delta.getContent() == null) {
                    return Flux.empty();
                }
                
                boolean isComplete = "stop".equals(choice.getFinish_reason());
                
                return Flux.just(ResponseMapper.toStreamChunk(
                    delta.getContent(),
                    choice.getIndex(),
                    isComplete
                ));
            });
    }
    
    @Override
    public boolean supportsStreaming() {
        return true;
    }

}
