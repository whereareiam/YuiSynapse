package me.whereareiam.yuisynapse.fluctlight;

import me.whereareiam.yui.fluctlight.FluctlightExtension;
import me.whereareiam.yuisynapse.model.SynapseUserData;

/**
 * Fluctlight extension for Synapse-specific user data.
 * <p>
 * This extension allows attaching conversation-specific data to Fluctlight instances,
 * such as current role, reputation, and metadata that is relevant for AI conversations.
 * <p>
 * Usage:
 * <pre>{@code
 * Fluctlight user = ...;
 * SynapseUserData data = user.getOrCreateExtension(SynapseFluctlightExtension.INSTANCE);
 * data.setReputation(100);
 * }</pre>
 */
public class SynapseFluctlightExtension implements FluctlightExtension<SynapseUserData> {
    /**
     * Singleton instance of the Synapse extension.
     */
    public static final SynapseFluctlightExtension INSTANCE = new SynapseFluctlightExtension();
    
    private static final String NAMESPACE = "synapse";
    
    private SynapseFluctlightExtension() {
        // Private constructor for singleton
    }
    
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
    
    @Override
    public Class<SynapseUserData> getDataType() {
        return SynapseUserData.class;
    }
    
    @Override
    public SynapseUserData createDefault() {
        return SynapseUserData.createDefault();
    }
}
