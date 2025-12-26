package me.whereareiam.yuisynapse.tool;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yuisynapse.model.SynapseMessage;

import java.util.List;
import java.util.Map;

/**
 * Context object provided to tools during session lifecycle events.
 * Contains session state, user information, and conversation history.
 */
@Data
@SuperBuilder
public class ToolContext {
    private String sessionId;
    private Fluctlight fromFluctlight;
    private String message;
    private List<SynapseMessage> history;
    private String currentMode;
    private Map<String, Object> sessionData;
}
