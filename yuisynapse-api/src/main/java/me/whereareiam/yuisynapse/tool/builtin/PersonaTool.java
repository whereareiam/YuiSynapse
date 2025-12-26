package me.whereareiam.yuisynapse.tool.builtin;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PersonaTool implements SynapseTool {
    private final String name;
    private final List<String> traits;
    private final String backstory;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getContextData(ToolContext context) {
        StringBuilder persona = new StringBuilder();
        persona.append("Your Personality:\n");
        persona.append("- Traits: ").append(String.join(", ", traits)).append("\n");
        if (backstory != null && !backstory.isEmpty()) {
            persona.append("- Backstory: ").append(backstory).append("\n");
        }
        return persona.toString();
    }
}
