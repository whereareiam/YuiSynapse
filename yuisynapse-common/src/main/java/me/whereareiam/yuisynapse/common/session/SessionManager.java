package me.whereareiam.yuisynapse.common.session;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.common.context.ConversationContext;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SessionManager {
    private final Map<String, DefaultSynapseSession> sessions = new ConcurrentHashMap<>();
    private final DefaultProviderRegistry providerRegistry;
    
    public String createSession(ConversationContext context) {
        String sessionId = UUID.randomUUID().toString();
        context.setSessionId(sessionId);
        DefaultSynapseSession session = new DefaultSynapseSession(context, providerRegistry);
        sessions.put(sessionId, session);
        return sessionId;
    }
    
    public DefaultSynapseSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public void removeSession(String sessionId) {
        DefaultSynapseSession session = sessions.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }
    
    public void cancelAllSessions() {
        sessions.values().forEach(DefaultSynapseSession::close);
        sessions.clear();
    }
    
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
