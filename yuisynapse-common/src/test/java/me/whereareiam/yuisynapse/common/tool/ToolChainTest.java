package me.whereareiam.yuisynapse.common.tool;

import me.whereareiam.yuisynapse.common.TestUtils;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolChainTest {
    
    @Test
    void shouldExecuteToolsInPriorityOrder() {
        // Given
        List<Integer> executionOrder = new ArrayList<>();
        
        SynapseTool tool1 = new TestTool("tool1", 50, executionOrder);
        SynapseTool tool2 = new TestTool("tool2", 10, executionOrder);
        SynapseTool tool3 = new TestTool("tool3", 100, executionOrder);
        
        ToolChain toolChain = new ToolChain(List.of(tool1, tool2, tool3));
        ToolContext context = TestUtils.mockContext();
        
        // When
        toolChain.executeBefore(context);
        
        // Then - should execute in priority order: 10, 50, 100
        assertThat(executionOrder).containsExactly(10, 50, 100);
    }
    
    @Test
    void shouldContinueOnToolFailure() {
        // Given
        List<String> executedTools = new ArrayList<>();
        
        SynapseTool tool1 = new SynapseTool() {
            @Override
            public String getName() { return "tool1"; }
            
            @Override
            public void beforeMessage(ToolContext context) {
                executedTools.add("tool1");
            }
            
            @Override
            public int getPriority() { return 10; }
        };
        
        SynapseTool tool2 = new SynapseTool() {
            @Override
            public String getName() { return "tool2-fails"; }
            
            @Override
            public void beforeMessage(ToolContext context) {
                executedTools.add("tool2");
                throw new RuntimeException("Tool 2 failed");
            }
            
            @Override
            public int getPriority() { return 20; }
        };
        
        SynapseTool tool3 = new SynapseTool() {
            @Override
            public String getName() { return "tool3"; }
            
            @Override
            public void beforeMessage(ToolContext context) {
                executedTools.add("tool3");
            }
            
            @Override
            public int getPriority() { return 30; }
        };
        
        ToolChain toolChain = new ToolChain(List.of(tool1, tool2, tool3));
        ToolContext context = TestUtils.mockContext();
        
        // When
        toolChain.executeBefore(context);
        
        // Then - tool3 should still execute despite tool2 failing
        assertThat(executedTools).containsExactly("tool1", "tool2", "tool3");
    }
    
    @Test
    void shouldInjectContextDataFromAllTools() {
        // Given
        SynapseTool tool1 = new SynapseTool() {
            @Override
            public String getName() { return "knowledge"; }
            
            @Override
            public String getContextData(ToolContext context) {
                return "Knowledge: Commands available";
            }
        };
        
        SynapseTool tool2 = new SynapseTool() {
            @Override
            public String getName() { return "user_info"; }
            
            @Override
            public String getContextData(ToolContext context) {
                return "User: Test User";
            }
        };
        
        SynapseTool tool3 = () -> "no_context";
        
        List<SynapseTool> tools = List.of(tool1, tool2, tool3);
        ToolContext context = TestUtils.mockContext();
        
        // When
        StringBuilder contextData = new StringBuilder();
        for (SynapseTool tool : tools) {
            String data = tool.getContextData(context);
            if (data != null) {
                contextData.append(data).append("\n");
            }
        }
        
        // Then
        String result = contextData.toString();
        assertThat(result).contains("Knowledge: Commands available");
        assertThat(result).contains("User: Test User");
    }
    
    @Test
    void shouldCallOnSessionStartForAllTools() {
        // Given
        List<String> initializedTools = new ArrayList<>();
        
        SynapseTool tool1 = new SynapseTool() {
            @Override
            public String getName() { return "tool1"; }
            
            @Override
            public void onSessionStart(ToolContext context) {
                initializedTools.add("tool1");
            }
        };
        
        SynapseTool tool2 = new SynapseTool() {
            @Override
            public String getName() { return "tool2"; }
            
            @Override
            public void onSessionStart(ToolContext context) {
                initializedTools.add("tool2");
            }
        };
        
        ToolChain toolChain = new ToolChain(List.of(tool1, tool2));
        ToolContext context = TestUtils.mockContext();
        
        // When
        toolChain.executeOnSessionStart(context);
        
        // Then
        assertThat(initializedTools).containsExactly("tool1", "tool2");
    }
    
    @Test
    void shouldExecuteAfterMessageForAllTools() {
        // Given
        List<String> executedTools = new ArrayList<>();
        
        SynapseTool tool1 = new SynapseTool() {
            @Override
            public String getName() { return "tool1"; }
            
            @Override
            public void afterMessage(ToolContext context, SynapseResponse response) {
                executedTools.add("tool1-after");
            }
        };
        
        SynapseTool tool2 = new SynapseTool() {
            @Override
            public String getName() { return "tool2"; }
            
            @Override
            public void afterMessage(ToolContext context, SynapseResponse response) {
                executedTools.add("tool2-after");
            }
        };
        
        ToolChain toolChain = new ToolChain(List.of(tool1, tool2));
        ToolContext context = TestUtils.mockContext();
        SynapseResponse response = SynapseResponse.builder()
                .content("AI response")
                .build();
        
        // When
        toolChain.executeAfter(context, response);
        
        // Then
        assertThat(executedTools).containsExactly("tool1-after", "tool2-after");
    }
    
    @Test
    void shouldHandleEmptyToolList() {
        // Given
        ToolChain toolChain = new ToolChain(List.of());
        ToolContext context = TestUtils.mockContext();
        
        // When/Then - should not throw
        toolChain.executeBefore(context);
        toolChain.executeOnSessionStart(context);
        toolChain.executeAfter(context, SynapseResponse.builder().build());
    }
    
    // Helper class
    private static class TestTool implements SynapseTool {
        private final String name;
        private final int priority;
        private final List<Integer> executionOrder;
        
        TestTool(String name, int priority, List<Integer> executionOrder) {
            this.name = name;
            this.priority = priority;
            this.executionOrder = executionOrder;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void beforeMessage(ToolContext context) {
            executionOrder.add(priority);
        }
        
        @Override
        public int getPriority() {
            return priority;
        }
    }
}
