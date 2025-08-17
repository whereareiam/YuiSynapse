package me.whereareiam.yuisynapse.tools.history;

import lombok.AllArgsConstructor;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.ToolFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HistoryFactory implements ToolFactory {
	private final ApplicationContext ctx;

	@Override
	public String name() {
		return "HISTORY";
	}

	@Override
	public Tool create() {
		return ctx.getBean(HistoryTool.class);
	}
}


