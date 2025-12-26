package me.whereareiam.yuisynapse.tool.builtin;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.model.SynapseMode;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ModeSwitchTool implements SynapseTool {
    private final String name;
    private final Map<String, SynapseMode> modes;
    private final Function<ToolContext, String> modeSelectorFunction;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void beforeMessage(ToolContext context) {
        String desiredMode = modeSelectorFunction.apply(context);
        
        if (desiredMode != null && !desiredMode.equals(context.getCurrentMode())) {
            context.getSessionData().put("switch_to_mode", desiredMode);
        }
    }
}
