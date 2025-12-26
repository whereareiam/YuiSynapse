package me.whereareiam.yuisynapse.common;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.fluctlight.SynapseFluctlightExtension;
import me.whereareiam.yuisynapse.model.SynapseUserData;
import me.whereareiam.yuisynapse.tool.ToolContext;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestUtils {
    public static ToolContext mockContext() {
        return ToolContext.builder()
                .sessionId("test-session-" + System.currentTimeMillis())
                .fromFluctlight(mockFluctlight())
                .message("test message")
                .history(new ArrayList<>())
                .currentMode("normal")
                .sessionData(new HashMap<>())
                .build();
    }
    
    public static ToolContext mockContextWithUser(Fluctlight user) {
        return ToolContext.builder()
                .sessionId("test-session-" + System.currentTimeMillis())
                .fromFluctlight(user)
                .message("test message")
                .history(new ArrayList<>())
                .currentMode("normal")
                .sessionData(new HashMap<>())
                .build();
    }
    
    public static ToolContext mockContextWithData(Map<String, Object> sessionData) {
        return ToolContext.builder()
                .sessionId("test-session-" + System.currentTimeMillis())
                .fromFluctlight(mockFluctlight())
                .message("test message")
                .history(new ArrayList<>())
                .currentMode("normal")
                .sessionData(sessionData)
                .build();
    }
    
    public static Fluctlight mockFluctlight() {
        User jdaUser = Mockito.mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(123456789L);
        when(jdaUser.getName()).thenReturn("Test User");
        
        Fluctlight fluctlight = new Fluctlight(jdaUser);
        
        // Add Synapse extension data
        SynapseUserData synapseData = new SynapseUserData();
        synapseData.setCurrentRole("member");
        synapseData.setRolePriority(10);
        synapseData.setReputation(100);
        synapseData.setMetadata(new HashMap<>());
        fluctlight.setExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), synapseData);
        
        return fluctlight;
    }
    
    public static Fluctlight mockFluctlightWithLanguage(String primaryLanguage, List<String> additionalLanguages) {
        User jdaUser = Mockito.mock(User.class);
        long userId = System.currentTimeMillis();
        when(jdaUser.getIdLong()).thenReturn(userId);
        when(jdaUser.getName()).thenReturn("Test User");
        
        // Normalize locale strings: replace hyphens with underscores for Discord
        DiscordLocale primaryLocale = DiscordLocale.from(primaryLanguage.replace("-", "_").toUpperCase());
        DiscordLocale[] additionalLocales = additionalLanguages.stream()
                .map(lang -> DiscordLocale.from(lang.replace("-", "_").toUpperCase()))
                .toArray(DiscordLocale[]::new);
        
        Fluctlight fluctlight = new Fluctlight(jdaUser, primaryLocale, additionalLocales, null);
        
        // Add Synapse extension data
        SynapseUserData synapseData = new SynapseUserData();
        synapseData.setCurrentRole("member");
        synapseData.setRolePriority(10);
        synapseData.setMetadata(new HashMap<>());
        fluctlight.setExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), synapseData);
        
        return fluctlight;
    }
    
    public static Fluctlight mockFluctlightWithMetadata(Map<String, Object> metadata) {
        User jdaUser = Mockito.mock(User.class);
        long userId = System.currentTimeMillis();
        when(jdaUser.getIdLong()).thenReturn(userId);
        when(jdaUser.getName()).thenReturn("Test User");
        
        Fluctlight fluctlight = new Fluctlight(jdaUser);
        
        // Add Synapse extension data with metadata
        SynapseUserData synapseData = new SynapseUserData();
        synapseData.setCurrentRole("member");
        synapseData.setRolePriority(10);
        synapseData.setMetadata(metadata);
        fluctlight.setExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), synapseData);
        
        return fluctlight;
    }
}
