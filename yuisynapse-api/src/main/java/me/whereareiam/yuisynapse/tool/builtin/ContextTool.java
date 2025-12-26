package me.whereareiam.yuisynapse.tool.builtin;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.util.function.Function;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ContextTool implements SynapseTool {
    private final String name;
    private final Function<ToolContext, String> contextProvider;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getContextData(ToolContext context) {
        return contextProvider.apply(context);
    }
}
