package me.whereareiam.yuisynapse.provider.openai.client;

import me.whereareiam.yuisynapse.limit.LimitEnforcer;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIRequest;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIResponse;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIStreamResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OpenAIHttpClient {
    private final ObjectProvider<SynapseSettings> settingsProvider;
    private final String providerId;
    private final LimitEnforcer limitEnforcer;
    private WebClient webClient;
    
    public OpenAIHttpClient(ObjectProvider<SynapseSettings> settingsProvider, String providerId, LimitEnforcer limitEnforcer) {
        this.settingsProvider = settingsProvider;
        this.providerId = providerId;
        this.limitEnforcer = limitEnforcer;
    }
    
    private WebClient getWebClient() {
        if (webClient == null) {
            var settings = settingsProvider.getObject();
            
            var config = settings.getProviders().get(providerId);
            if (config == null) {
                throw new IllegalStateException("Provider config not found for: " + providerId);
            }
            
            webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .defaultHeader("Authorization", "Bearer " + config.getKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
        }
        return webClient;
    }
    
    public void reload() {
        webClient = null;
    }
    
    public Mono<OpenAIResponse> sendRequest(OpenAIRequest request) {
        WebClient client = getWebClient();
        
        Mono<Void> rateLimitMono = limitEnforcer != null 
            ? limitEnforcer.acquireRequest() 
            : Mono.empty();
        
        return rateLimitMono
            .then(client.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIResponse.class))
            .doFinally(_ -> {
                if (limitEnforcer != null) {
                    limitEnforcer.releaseRequest();
                }
            });
    }
    
    public Flux<OpenAIStreamResponse> streamRequest(OpenAIRequest request) {
        WebClient client = getWebClient();
        
        Mono<Void> rateLimitMono = limitEnforcer != null 
            ? limitEnforcer.acquireRequest() 
            : Mono.empty();
        
        return rateLimitMono
            .thenMany(client.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(OpenAIStreamResponse.class))
            .doFinally(_ -> {
                if (limitEnforcer != null) {
                    limitEnforcer.releaseRequest();
                }
            });
    }
}
