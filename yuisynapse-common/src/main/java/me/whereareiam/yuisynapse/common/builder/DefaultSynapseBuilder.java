package me.whereareiam.yuisynapse.common.builder;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.SynapseBuilder;
import me.whereareiam.yuisynapse.common.config.provider.SynapseSettingsProvider;
import me.whereareiam.yuisynapse.common.context.ConversationContext;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import me.whereareiam.yuisynapse.common.session.DefaultSynapseSession;
import me.whereareiam.yuisynapse.common.session.SessionManager;
import me.whereareiam.yuisynapse.common.tool.ToolChain;
import me.whereareiam.yuisynapse.model.*;
import me.whereareiam.yuisynapse.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.provider.SynapseProvider;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.request.SynapseSingleMessageRequest;
import me.whereareiam.yuisynapse.session.SynapseSession;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class DefaultSynapseBuilder implements SynapseBuilder {
    private final DefaultProviderRegistry providerRegistry;
    private final SynapseSettingsProvider settingsProvider;
    private final SessionManager sessionManager;
    
    private boolean isMessage = false;
    private boolean isConversation = false;
    private String systemPrompt = "";
    private String providerId;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private final List<Fluctlight> users = new ArrayList<>();
    private final List<SynapseTool> tools = new ArrayList<>();
    private final List<String> registeredModeNames = new ArrayList<>();
    private final List<SynapseMode> registeredModeObjects = new ArrayList<>();
    private String currentModeName;
    private SynapseMode currentMode;
    private boolean streamingEnabled = false;
    private Consumer<StreamChunk> streamCallback;
    private Consumer<SynapseResponse> responseCallback;
    private Consumer<SynapseError> errorCallback;
    
    @Override
    public SynapseBuilder message() {
        this.isMessage = true;
        this.isConversation = false;
        return this;
    }
    
    @Override
    public SynapseBuilder conversation() {
        this.isConversation = true;
        this.isMessage = false;
        return this;
    }
    
    @Override
    public SynapseBuilder withSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
        return this;
    }
    
    @Override
    public SynapseBuilder withProvider(String providerId) {
        this.providerId = providerId;
        return this;
    }
    
    @Override
    public SynapseBuilder withModel(String model) {
        this.model = model;
        return this;
    }
    
    @Override
    public SynapseBuilder withTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }
    
    @Override
    public SynapseBuilder withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
    
    @Override
    public SynapseBuilder withUser(Fluctlight user) {
        this.users.add(user);
        return this;
    }
    
    @Override
    public SynapseBuilder withUsers(Fluctlight... users) {
        this.users.addAll(Arrays.asList(users));
        return this;
    }
    
    @Override
    public SynapseBuilder registerMode(String name, SynapseMode mode) {
        this.registeredModeNames.add(name);
        this.registeredModeObjects.add(mode);
        return this;
    }
    
    @Override
    public SynapseBuilder withMode(String modeName) {
        this.currentModeName = modeName;
        return this;
    }
    
    @Override
    public SynapseBuilder withMode(SynapseMode mode) {
        this.currentMode = mode;
        return this;
    }
    
    @Override
    public SynapseBuilder withTool(SynapseTool tool) {
        this.tools.add(tool);
        return this;
    }
    
    @Override
    public SynapseBuilder withTools(SynapseTool... tools) {
        this.tools.addAll(Arrays.asList(tools));
        return this;
    }
    
    @Override
    public SynapseBuilder enableStreaming() {
        this.streamingEnabled = true;
        return this;
    }
    
    @Override
    public SynapseBuilder onStream(Consumer<StreamChunk> callback) {
        this.streamCallback = callback;
        return this;
    }
    
    @Override
    public SynapseBuilder onResponse(Consumer<SynapseResponse> callback) {
        this.responseCallback = callback;
        return this;
    }
    
    @Override
    public SynapseBuilder onError(Consumer<SynapseError> callback) {
        this.errorCallback = callback;
        return this;
    }
    
    @Override
    public CompletableFuture<SynapseResponse> send(String message) {
        return send(message, null);
    }
    
    @Override
    public CompletableFuture<SynapseResponse> send(String message, String fromUserId) {
        if (isMessage) return sendSingleMessage(message);
        if (isConversation) return start().thenCompose(session -> session.send(message, fromUserId));
        return sendSingleMessage(message);
    }
    
    private CompletableFuture<SynapseResponse> sendSingleMessage(String message) {
        SynapseProvider provider = getProvider();
        
        SynapseSingleMessageRequest request = SynapseSingleMessageRequest.builder()
            .systemPrompt(systemPrompt)
            .message(message)
            .model(getEffectiveModel())
            .temperature(getEffectiveTemperature())
            .maxTokens(getEffectiveMaxTokens())
            .build();
        
        CompletableFuture<SynapseResponse> future;
        
        if (streamingEnabled && provider.supportsStreaming()) {
            future = handleStreamingRequest(provider);
        } else {
            future = provider.sendSingleMessage(request);
        }
        
        return future
            .thenApply(response -> {
                if (responseCallback != null) responseCallback.accept(response);
                return response;
            })
            .exceptionally(throwable -> {
                if (errorCallback != null) {
                    SynapseError error = SynapseError.builder()
                        .message(throwable.getMessage())
                        .cause(throwable)
                        .timestamp(Instant.now())
                        .build();
                    errorCallback.accept(error);
                }
                throw new RuntimeException(throwable);
            });
    }
    
    private CompletableFuture<SynapseResponse> handleStreamingRequest(SynapseProvider provider) {
        SynapseConversationRequest streamRequest = SynapseConversationRequest.builder()
            .systemPrompt(systemPrompt)
            .history(List.of())
            .model(getEffectiveModel())
            .temperature(getEffectiveTemperature())
            .maxTokens(getEffectiveMaxTokens())
            .stream(true)
            .build();
        
        StringBuilder fullContent = new StringBuilder();
        CompletableFuture<SynapseResponse> future = new CompletableFuture<>();
        
        provider.streamConversation(streamRequest)
            .doOnNext(chunk -> {
                fullContent.append(chunk.getContent());
                if (streamCallback != null) streamCallback.accept(chunk);
            })
            .doOnComplete(() -> {
                SynapseResponse response = SynapseResponse.builder()
                    .content(fullContent.toString())
                    .isStreaming(true)
                    .build();
                future.complete(response);
            })
            .doOnError(future::completeExceptionally)
            .subscribe();
        
        return future;
    }
    
    @Override
    public CompletableFuture<SynapseSession> start() {
        if (!isConversation) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Cannot start session in message mode. Use .conversation() first.")
            );
        }
        
        ConversationContext context = new ConversationContext(null);
        context.setSystemPrompt(systemPrompt);
        context.setProviderId(providerId);
        context.setModel(getEffectiveModel());
        context.setTemperature(getEffectiveTemperature());
        context.setMaxTokens(getEffectiveMaxTokens());
        
        users.forEach(context::addUser);
        
        for (int i = 0; i < registeredModeNames.size(); i++) {
            context.registerMode(registeredModeNames.get(i), registeredModeObjects.get(i));
        }
        
        if (currentModeName != null) {
            context.setCurrentMode(currentModeName);
        } else if (currentMode != null) {
            String modeName = currentMode.getName() != null ? currentMode.getName() : "default";
            context.registerMode(modeName, currentMode);
            context.setCurrentMode(modeName);
        }
        
        context.getTools().addAll(tools);
        
        String sessionId = sessionManager.createSession(context);
        DefaultSynapseSession session = sessionManager.getSession(sessionId);
        
        ToolContext toolContext = ToolContext.builder()
            .sessionId(sessionId)
            .sessionData(context.getSessionData())
            .build();
        
        ToolChain toolChain = new ToolChain(tools);
        toolChain.executeOnSessionStart(toolContext);
        
        return CompletableFuture.completedFuture(session);
    }
    
    private SynapseProvider getProvider() {
        if (providerId != null) return providerRegistry.getProvider(providerId);
        return providerRegistry.getDefaultProvider();
    }
    
    private String getEffectiveModel() {
        if (model != null) return model;
        
        SynapseSettings settings = settingsProvider.get();
        String effectiveProviderId = providerId != null ? providerId : settings.getDefaultProvider();
        SynapseSettings.ProviderConfig providerConfig = settings.getProviders().get(effectiveProviderId);
        
        if (providerConfig != null && providerConfig.getDefaultModel() != null) {
            return providerConfig.getDefaultModel();
        }
        
        return "gpt-4";
    }
    
    private Double getEffectiveTemperature() {
        return temperature != null ? temperature : 0.7;
    }
    
    private Integer getEffectiveMaxTokens() {
        return maxTokens != null ? maxTokens : 1000;
    }
}
