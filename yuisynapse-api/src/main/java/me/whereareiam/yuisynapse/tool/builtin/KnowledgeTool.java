package me.whereareiam.yuisynapse.tool.builtin;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class KnowledgeTool implements SynapseTool {
    private final String name;
    private final String knowledge;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getContextData(ToolContext context) {
        return knowledge;
    }
}
