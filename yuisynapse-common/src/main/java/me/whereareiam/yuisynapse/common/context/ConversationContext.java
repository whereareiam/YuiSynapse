package me.whereareiam.yuisynapse.common.context;

import lombok.Data;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.model.SynapseMessage;
import me.whereareiam.yuisynapse.model.SynapseMode;
import me.whereareiam.yuisynapse.tool.SynapseTool;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ConversationContext {
    private String sessionId;
    private String systemPrompt;
    private List<SynapseMessage> history;
    private Map<String, Fluctlight> users;
    private Map<String, SynapseMode> registeredModes;
    private String currentMode;
    private List<SynapseTool> tools;
    private Map<String, Object> sessionData;
    private String providerId;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Instant createdAt;
    private Instant lastActivityAt;
    private boolean isActive;
    
    public ConversationContext(String sessionId) {
        this.sessionId = sessionId;
        this.history = new ArrayList<>();
        this.users = new ConcurrentHashMap<>();
        this.registeredModes = new HashMap<>();
        this.tools = new ArrayList<>();
        this.sessionData = new ConcurrentHashMap<>();
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
        this.isActive = true;
    }
    
    public void addMessage(SynapseMessage message) {
        history.add(message);
        lastActivityAt = Instant.now();
    }
    
    public void addUser(Fluctlight user) {
        users.put(String.valueOf(user.getId()), user);
    }
    
    public void removeUser(String userId) {
        users.remove(userId);
    }
    
    public Fluctlight getUser(String userId) {
        return users.get(userId);
    }
    
    public void registerMode(String name, SynapseMode mode) {
        registeredModes.put(name, mode);
    }
    
    public SynapseMode getMode(String name) {
        return registeredModes.get(name);
    }
    
    public void updateActivity() {
        lastActivityAt = Instant.now();
    }
}
