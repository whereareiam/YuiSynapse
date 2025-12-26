package me.whereareiam.yuisynapse.tool.builtin;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.fluctlight.SynapseFluctlightExtension;
import me.whereareiam.yuisynapse.model.SynapseUserData;
import me.whereareiam.yuisynapse.tool.SynapseTool;
import me.whereareiam.yuisynapse.tool.ToolContext;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class UserInfoTool implements SynapseTool {
    @Override
    public String getName() {
        return "user_info";
    }
    
    @Override
    public String getContextData(ToolContext context) {
        Fluctlight user = context.getFromFluctlight();
        if (user == null) {
            return null;
        }
        
        SynapseUserData synapseData = user.getExtension(
            SynapseFluctlightExtension.INSTANCE.getNamespace(),
            SynapseUserData.class
        );
        
        StringBuilder info = new StringBuilder();
        info.append("User Information:\n");
        info.append("- Name: ").append(user.getName()).append("\n");
        info.append("- ID: ").append(user.getId()).append("\n");
        
        if (user.getPrimaryLanguage() != null) {
            info.append("- Primary Language: ").append(user.getPrimaryLanguage().getLocale()).append("\n");
            info.append("  → Respond primarily in this language\n");
        }
        
        if (user.getAdditionalLanguages() != null && user.getAdditionalLanguages().length > 0) {
            String additionalLangs = Arrays.stream(user.getAdditionalLanguages())
                .map(DiscordLocale::getLocale)
                .collect(Collectors.joining(", "));
            info.append("- Additional Languages: ").append(additionalLangs).append("\n");
            info.append("  → User can understand these languages too\n");
        }
        
        if (synapseData != null) {
            if (synapseData.getCurrentRole() != null) {
                info.append("- Role: ").append(synapseData.getCurrentRole())
                    .append(" (priority: ").append(synapseData.getRolePriority()).append(")\n");
            }
            
            if (synapseData.getReputation() > 0) {
                info.append("- Reputation: ").append(synapseData.getReputation()).append("/100\n");
            }
            
            if (synapseData.getMetadata() != null && !synapseData.getMetadata().isEmpty()) {
                info.append("- Additional Info:\n");
                synapseData.getMetadata().forEach((key, value) ->
                    info.append("  - ").append(key).append(": ").append(value).append("\n")
                );
            }
        }
        
        return info.toString();
    }
}
