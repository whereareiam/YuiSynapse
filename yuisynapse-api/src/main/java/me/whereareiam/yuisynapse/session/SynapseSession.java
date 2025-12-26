package me.whereareiam.yuisynapse.session;

import me.whereareiam.yuisynapse.model.SynapseMessage;
import me.whereareiam.yuisynapse.model.SynapseResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an active conversation session with an AI.
 * Manages conversation history, users, and mode switching.
 */
@SuppressWarnings("unused")
public interface SynapseSession {
    /**
     * @return unique identifier for this session
     */
    String getSessionId();
    
    /**
     * Sends a message to the AI in this session.
     * 
     * @param message the message content
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> send(String message);
    
    /**
     * Sends a message from a specific user to the AI.
     * 
     * @param message the message content
     * @param fromUserId the user ID sending the message
     * @return future containing the AI response
     */
    CompletableFuture<SynapseResponse> send(String message, String fromUserId);
    
    /**
     * Switches to a different conversation mode.
     * 
     * @param modeName the name of the mode to activate
     */
    void switchMode(String modeName);
    
    /**
     * @return name of the currently active mode
     */
    String getCurrentMode();
    
    /**
     * Adds a user to this conversation session.
     * 
     * @param user the user to add
     */
    void addUser(me.whereareiam.yui.model.fluctlight.Fluctlight user);
    
    /**
     * Removes a user from this conversation session.
     * 
     * @param userId the ID of the user to remove
     */
    void removeUser(String userId);
    
    /**
     * @return complete conversation history
     */
    List<SynapseMessage> getHistory();
    
    /**
     * Retrieves recent conversation history.
     * 
     * @param limit maximum number of messages to retrieve
     * @return limited conversation history
     */
    List<SynapseMessage> getHistory(int limit);
    
    /**
     * Closes this session and releases resources.
     */
    void close();
    
    /**
     * @return true if this session is still active
     */
    boolean isActive();
}
