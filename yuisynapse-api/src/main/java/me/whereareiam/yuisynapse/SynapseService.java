package me.whereareiam.yuisynapse;

import me.whereareiam.yuisynapse.session.SynapseSession;

/**
 * Main service interface for interacting with the Synapse AI system.
 * Provides builder access and session management.
 */
@SuppressWarnings("unused")
public interface SynapseService {
    /**
     * @return new builder for creating AI interactions
     */
    SynapseBuilder builder();
    
    /**
     * Cancels an active session.
     * 
     * @param sessionId the session ID to cancel
     */
    void cancelSession(String sessionId);
    
    /**
     * Retrieves an active session by ID.
     * 
     * @param sessionId the session ID to retrieve
     * @return the session, or null if not found
     */
    SynapseSession getSession(String sessionId);
    
    /**
     * Cancels all active sessions.
     */
    void cancelAllSessions();
}
