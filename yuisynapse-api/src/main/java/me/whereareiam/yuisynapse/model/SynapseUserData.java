package me.whereareiam.yuisynapse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension data for Fluctlight that adds Synapse-specific user information.
 * <p>
 * This data is stored in-memory only and is not persisted to the database.
 * It includes conversation-specific context like the user's current role,
 * priority, reputation, and arbitrary metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SynapseUserData {
    /**
     * The user's current role in the conversation context (e.g., "admin", "moderator", "member")
     */
    private String currentRole;
    
    /**
     * The priority level of the user's role (higher = more priority)
     */
    private int rolePriority;
    
    /**
     * The user's reputation score
     */
    private int reputation;
    
    /**
     * Additional metadata specific to the conversation or session
     */
    private Map<String, Object> metadata;
    
    /**
     * Creates default data with empty values.
     */
    public static SynapseUserData createDefault() {
        return new SynapseUserData(null, 0, 0, new HashMap<>());
    }
}
