package me.whereareiam.yuisynapse.common.tool.builtin;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.common.TestUtils;
import me.whereareiam.yuisynapse.fluctlight.SynapseFluctlightExtension;
import me.whereareiam.yuisynapse.model.SynapseUserData;
import me.whereareiam.yuisynapse.tool.ToolContext;
import me.whereareiam.yuisynapse.tool.builtin.UserInfoTool;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserInfoToolTest {
    
    @Test
    void shouldFormatBasicUserInfo() {
        // Given
        User jdaUser = mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(123L);
        when(jdaUser.getName()).thenReturn("John Doe");
        
        Fluctlight user = new Fluctlight(jdaUser);
        SynapseUserData synapseData = new SynapseUserData();
        synapseData.setCurrentRole("member");
        synapseData.setRolePriority(10);
        synapseData.setReputation(85);
        user.setExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), synapseData);
        
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info)
                .contains("Name: John Doe")
                .contains("ID: 123")
                .contains("Role: member (priority: 10)")
                .contains("Reputation: 85/100");
    }
    
    @Test
    void shouldFormatUserLanguageInfo() {
        // Given
        Fluctlight user = TestUtils.mockFluctlightWithLanguage("es-ES", List.of("en-US", "fr-FR"));
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then - DiscordLocale will format as underscore-separated uppercase (e.g., ES_ES)
        assertThat(info)
                .contains("Primary Language:")
                .contains("→ Respond primarily in this language")
                .contains("Additional Languages:")
                .contains("→ User can understand these languages too");
    }
    
    @Test
    void shouldIncludeMetadata() {
        // Given
        Map<String, Object> metadata = Map.of(
                "timezone", "UTC-5",
                "country", "USA",
                "premium", true
        );
        Fluctlight user = TestUtils.mockFluctlightWithMetadata(metadata);
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info)
                .contains("Additional Info:")
                .contains("timezone: UTC-5")
                .contains("country: USA")
                .contains("premium: true");
    }
    
    @Test
    void shouldHandleMissingLanguageFields() {
        // Given
        User jdaUser = mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(456L);
        when(jdaUser.getName()).thenReturn("Jane Doe");
        
        Fluctlight user = new Fluctlight(jdaUser);
        
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info)
                .contains("Name: Jane Doe")
                .doesNotContain("Primary Language")
                .doesNotContain("Additional Languages");
    }
    
    @Test
    void shouldHandleNullUser() {
        // Given
        ToolContext context = ToolContext.builder()
                .sessionId("test-session")
                .fromFluctlight(null)
                .build();
        
        UserInfoTool tool = new UserInfoTool();
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info).isNull();
    }
    
    @Test
    void shouldHandleEmptyAdditionalLanguages() {
        // Given
        User jdaUser = mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(789L);
        when(jdaUser.getName()).thenReturn("Test User");
        
        Fluctlight user = new Fluctlight(jdaUser, DiscordLocale.ENGLISH_US, new DiscordLocale[0], null);
        
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info)
                .contains("Primary Language: en-US")
                .doesNotContain("Additional Languages:");
    }
    
    @Test
    void shouldNotShowReputationWhenZero() {
        // Given
        User jdaUser = mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(0L);
        when(jdaUser.getName()).thenReturn("New User");
        
        Fluctlight user = new Fluctlight(jdaUser);
        SynapseUserData synapseData = new SynapseUserData();
        synapseData.setReputation(0);
        user.setExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), synapseData);
        
        UserInfoTool tool = new UserInfoTool();
        ToolContext context = TestUtils.mockContextWithUser(user);
        
        // When
        String info = tool.getContextData(context);
        
        // Then
        assertThat(info)
                .contains("Name: New User")
                .doesNotContain("Reputation:");
    }
    
    @Test
    void shouldReturnCorrectToolName() {
        // Given
        UserInfoTool tool = new UserInfoTool();
        
        // When
        String name = tool.getName();
        
        // Then
        assertThat(name).isEqualTo("user_info");
    }
}
