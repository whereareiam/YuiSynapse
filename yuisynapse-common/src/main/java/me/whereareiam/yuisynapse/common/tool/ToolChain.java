package me.whereareiam.yuisynapse.common.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ToolChain {
    private final List<SynapseTool> tools;
    
    public void executeBefore(ToolContext context) {
        List<SynapseTool> sortedTools = tools.stream()
            .sorted(Comparator.comparingInt(SynapseTool::getPriority))
            .toList();
        
        for (SynapseTool tool : sortedTools) {
            try {
                tool.beforeMessage(context);
            } catch (Exception e) {
                log.error("Error executing tool {}: {}", tool.getName(), e.getMessage(), e);
            }
        }
    }
    
    public void executeAfter(ToolContext context, SynapseResponse response) {
        for (SynapseTool tool : tools) {
            try {
                tool.afterMessage(context, response);
            } catch (Exception e) {
                log.error("Error executing tool {} (after): {}", tool.getName(), e.getMessage(), e);
            }
        }
    }
    
    public void executeOnSessionStart(ToolContext context) {
        for (SynapseTool tool : tools) {
            try {
                tool.onSessionStart(context);
            } catch (Exception e) {
                log.error("Error initializing tool {}: {}", tool.getName(), e.getMessage(), e);
            }
        }
    }
}
