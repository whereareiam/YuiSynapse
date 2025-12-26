package me.whereareiam.yuisynapse.common.tool.builtin;

import me.whereareiam.yuisynapse.common.TestUtils;
import me.whereareiam.yuisynapse.fluctlight.SynapseFluctlightExtension;
import me.whereareiam.yuisynapse.model.SynapseUserData;
import me.whereareiam.yuisynapse.tool.ToolContext;
import me.whereareiam.yuisynapse.tool.builtin.ContextTool;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ContextToolTest {
    
    @Test
    void shouldExecuteCustomContextProvider() {
        // Given
        Function<ToolContext, String> provider = ctx ->
                "Server: " + ctx.getSessionData().get("server");
        
        ContextTool tool = new ContextTool("server_info", provider);
        
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("server", "TestServer");
        ToolContext context = TestUtils.mockContextWithData(sessionData);
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).isEqualTo("Server: TestServer");
    }
    
    @Test
    void shouldProvideTimestampContext() {
        // Given
        Function<ToolContext, String> provider = _ ->
                "Current Time: " + Instant.now().toString();
        
        ContextTool tool = new ContextTool("timestamp", provider);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).startsWith("Current Time:");
    }
    
    @Test
    void shouldProvideMultilineContext() {
        // Given
        Function<ToolContext, String> provider = ctx -> String.format("""
                Environment Information:
                - Session ID: %s
                - User: %s
                - Message Count: %d
                """,
                ctx.getSessionId(),
                ctx.getFromFluctlight().getName(),
                ctx.getHistory().size()
        );
        
        ContextTool tool = new ContextTool("environment", provider);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result)
                .contains("Environment Information:")
                .contains("Session ID: " + context.getSessionId())
                .contains("User: Test User")
                .contains("Message Count: 0");
    }
    
    @Test
    void shouldAccessSessionDataInProvider() {
        // Given
        Function<ToolContext, String> provider = ctx -> {
            String serverName = (String) ctx.getSessionData().get("serverName");
            int activeUsers = (int) ctx.getSessionData().getOrDefault("activeUsers", 0);
            return String.format("Server: %s, Active Users: %d", serverName, activeUsers);
        };
        
        ContextTool tool = new ContextTool("server_stats", provider);
        
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("serverName", "Gaming Server");
        sessionData.put("activeUsers", 150);
        ToolContext context = TestUtils.mockContextWithData(sessionData);
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).isEqualTo("Server: Gaming Server, Active Users: 150");
    }
    
    @Test
    void shouldReturnNullWhenProviderReturnsNull() {
        // Given
        Function<ToolContext, String> provider = _ -> null;
        ContextTool tool = new ContextTool("null_provider", provider);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void shouldReturnEmptyStringWhenProviderReturnsEmpty() {
        // Given
        Function<ToolContext, String> provider = _ -> "";
        ContextTool tool = new ContextTool("empty_provider", provider);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldReturnCorrectToolName() {
        // Given
        Function<ToolContext, String> provider = _ -> "test";
        ContextTool tool = new ContextTool("custom_context", provider);
        
        // When
        String name = tool.getName();
        
        // Then
        assertThat(name).isEqualTo("custom_context");
    }
    
    @Test
    void shouldHandleComplexLogicInProvider() {
        // Given
        Function<ToolContext, String> provider = ctx -> {
            var synapseData = ctx.getFromFluctlight().getExtension(SynapseFluctlightExtension.INSTANCE.getNamespace(), SynapseUserData.class);
            int reputation = synapseData != null ? synapseData.getReputation() : 0;
            if (reputation >= 80) {
                return "User Tier: Premium";
            } else if (reputation >= 50) {
                return "User Tier: Standard";
            } else {
                return "User Tier: Basic";
            }
        };
        
        ContextTool tool = new ContextTool("user_tier", provider);
        ToolContext context = TestUtils.mockContext(); // Default user has reputation 100
        
        // When
        String result = tool.getContextData(context);
        
        // Then
        assertThat(result).isEqualTo("User Tier: Premium");
    }
}
