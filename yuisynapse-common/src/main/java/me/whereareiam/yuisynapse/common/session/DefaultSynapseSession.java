package me.whereareiam.yuisynapse.common.session;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.common.context.ConversationContext;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import me.whereareiam.yuisynapse.common.tool.ToolChain;
import me.whereareiam.yuisynapse.model.SynapseMessage;
import me.whereareiam.yuisynapse.model.SynapseMode;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.provider.SynapseProvider;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.session.SynapseSession;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DefaultSynapseSession implements SynapseSession {
    private final ConversationContext context;
	private final DefaultProviderRegistry providerRegistry;
    
    @Override
    public String getSessionId() {
        return context.getSessionId();
    }
    
    @Override
    public CompletableFuture<SynapseResponse> send(String message) {
        return send(message, null);
    }
    
    @Override
    public CompletableFuture<SynapseResponse> send(String message, String fromUserId) {
        if (!context.isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Session is not active")
            );
        }
        
        Fluctlight fromUser = fromUserId != null ? context.getUser(fromUserId) : null;
        
        // Create tool context
        ToolContext toolContext = ToolContext.builder()
            .sessionId(context.getSessionId())
            .fromFluctlight(fromUser)
            .message(message)
            .history(context.getHistory())
            .currentMode(context.getCurrentMode())
            .sessionData(context.getSessionData())
            .build();
        
        // Execute before-message tools
        ToolChain toolChain = new ToolChain(context.getTools());
        toolChain.executeBefore(toolContext);
        
        // Check if we should skip AI
        if (Boolean.TRUE.equals(context.getSessionData().get("skip_ai"))) {
            String cachedResponse = (String) context.getSessionData().get("cached_response");
            if (cachedResponse != null) {
                SynapseResponse response = SynapseResponse.builder()
                    .content(cachedResponse)
                    .sessionId(context.getSessionId())
                    .build();
                    
                toolChain.executeAfter(toolContext, response);
                return CompletableFuture.completedFuture(response);
            }
        }
        
        // Add user message to history
        SynapseMessage userMessage = SynapseMessage.builder()
            .role("user")
            .content(message)
            .userId(fromUserId)
            .timestamp(Instant.now())
            .build();
        context.addMessage(userMessage);
        
        // Build system prompt with tool context
        String enrichedSystemPrompt = buildEnrichedSystemPrompt(toolContext);
        
        // Apply mode modifiers
        Double effectiveTemperature = context.getTemperature();
        Integer effectiveMaxTokens = context.getMaxTokens();
        
        if (context.getCurrentMode() != null) {
            SynapseMode mode = context.getMode(context.getCurrentMode());
            if (mode != null) {
                if (mode.getTemperatureModifier() != null) {
                    effectiveTemperature = mode.getTemperatureModifier();
                }
                if (mode.getMaxTokensModifier() != null) {
                    effectiveMaxTokens = mode.getMaxTokensModifier();
                }
            }
        }
        
        // Build request
        SynapseConversationRequest request = SynapseConversationRequest.builder()
            .systemPrompt(enrichedSystemPrompt)
            .history(context.getHistory())
            .users(new ArrayList<>(context.getUsers().values()))
            .model(context.getModel())
            .temperature(effectiveTemperature)
            .maxTokens(effectiveMaxTokens)
            .stream(false)
            .build();
        
        // Get provider and send
        SynapseProvider provider = getProvider();
        
        return provider.sendConversation(request).thenApply(response -> {
            // Add AI response to history
            SynapseMessage aiMessage = SynapseMessage.builder()
                .role("assistant")
                .content(response.getContent())
                .timestamp(Instant.now())
                .build();
            context.addMessage(aiMessage);
            
            // Execute after-message tools
            toolChain.executeAfter(toolContext, response);
            
            // Check for mode switch
            String switchToMode = (String) context.getSessionData().get("switch_to_mode");
            if (switchToMode != null) {
                switchMode(switchToMode);
                context.getSessionData().remove("switch_to_mode");
            }
            
            context.updateActivity();
            return response;
        });
    }
    
    private String buildEnrichedSystemPrompt(ToolContext toolContext) {
        StringBuilder prompt = new StringBuilder(context.getSystemPrompt());
        
        // Add mode modifier if present
        if (context.getCurrentMode() != null) {
            SynapseMode mode = context.getMode(context.getCurrentMode());
            if (mode != null && mode.getSystemPromptModifier() != null) {
                prompt.append("\n\n").append(mode.getSystemPromptModifier());
            }
        }
        
        // Add tool context data
        for (me.whereareiam.yuisynapse.tool.SynapseTool tool : context.getTools()) {
            String contextData = tool.getContextData(toolContext);
            if (contextData != null && !contextData.isEmpty()) {
                prompt.append("\n\n").append(contextData);
            }
        }
        
        return prompt.toString();
    }
    
    private SynapseProvider getProvider() {
        if (context.getProviderId() != null) {
            return providerRegistry.getProvider(context.getProviderId());
        }
        return providerRegistry.getDefaultProvider();
    }
    
    @Override
    public void switchMode(String modeName) {
        if (context.getRegisteredModes().containsKey(modeName)) {
            context.setCurrentMode(modeName);
        }
    }
    
    @Override
    public String getCurrentMode() {
        return context.getCurrentMode();
    }
    
    @Override
    public void addUser(Fluctlight user) {
        context.addUser(user);
    }
    
    @Override
    public void removeUser(String userId) {
        context.removeUser(userId);
    }
    
    @Override
    public List<SynapseMessage> getHistory() {
        return context.getHistory();
    }
    
    @Override
    public List<SynapseMessage> getHistory(int limit) {
        List<SynapseMessage> history = context.getHistory();
        int size = history.size();
        if (size <= limit) {
            return history;
        }
        return history.subList(size - limit, size);
    }
    
    @Override
    public void close() {
        context.setActive(false);
    }
    
    @Override
    public boolean isActive() {
        return context.isActive();
    }
}
