package me.whereareiam.yuisynapse.common.tool.builtin;

import me.whereareiam.yuisynapse.common.TestUtils;
import me.whereareiam.yuisynapse.tool.ToolContext;
import me.whereareiam.yuisynapse.tool.builtin.KnowledgeTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeToolTest {
    
    @Test
    void shouldInjectKnowledgeIntoContext() {
        // Given
        String knowledge = "Available Commands:\n/ban <user> <reason> - Ban a user\n/kick <user> - Kick a user";
        KnowledgeTool tool = new KnowledgeTool("help_docs", knowledge);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String contextData = tool.getContextData(context);
        
        // Then
        assertThat(contextData).isEqualTo(knowledge);
    }
    
    @Test
    void shouldReturnToolName() {
        // Given
        KnowledgeTool tool = new KnowledgeTool("custom_name", "some knowledge");
        
        // When
        String name = tool.getName();
        
        // Then
        assertThat(name).isEqualTo("custom_name");
    }
    
    @Test
    void shouldHandleEmptyKnowledge() {
        // Given
        KnowledgeTool tool = new KnowledgeTool("empty", "");
        ToolContext context = TestUtils.mockContext();
        
        // When
        String contextData = tool.getContextData(context);
        
        // Then
        assertThat(contextData).isEmpty();
    }
    
    @Test
    void shouldHandleMultilineKnowledge() {
        // Given
        String multilineKnowledge = """
                Server Rules:
                1. Be respectful to all members
                2. No spam or advertisements
                3. No NSFW content
                
                Violation Consequences:
                - First offense: Warning
                - Second offense: Temporary ban
                - Third offense: Permanent ban
                """;
        KnowledgeTool tool = new KnowledgeTool("rules", multilineKnowledge);
        ToolContext context = TestUtils.mockContext();
        
        // When
        String contextData = tool.getContextData(context);
        
        // Then
        assertThat(contextData)
                .contains("Server Rules:")
                .contains("No spam")
                .contains("Permanent ban");
    }
}
