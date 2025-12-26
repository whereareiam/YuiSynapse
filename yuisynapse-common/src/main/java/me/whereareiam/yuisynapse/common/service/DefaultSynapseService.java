package me.whereareiam.yuisynapse.common.service;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.SynapseBuilder;
import me.whereareiam.yuisynapse.SynapseService;
import me.whereareiam.yuisynapse.common.builder.DefaultSynapseBuilder;
import me.whereareiam.yuisynapse.common.config.provider.SynapseSettingsProvider;
import me.whereareiam.yuisynapse.common.provider.DefaultProviderRegistry;
import me.whereareiam.yuisynapse.common.session.SessionManager;
import me.whereareiam.yuisynapse.session.SynapseSession;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSynapseService implements SynapseService {
    private final SessionManager sessionManager;
    private final DefaultProviderRegistry providerRegistry;
    private final SynapseSettingsProvider settingsProvider;
    
    @Override
    public SynapseBuilder builder() {
        return new DefaultSynapseBuilder(providerRegistry, settingsProvider, sessionManager);
    }
    
    @Override
    public void cancelSession(String sessionId) {
        sessionManager.removeSession(sessionId);
    }
    
    @Override
    public SynapseSession getSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }
    
    @Override
    public void cancelAllSessions() {
        sessionManager.cancelAllSessions();
    }
}
