package me.whereareiam.yuisynapse.tools.interest;

import lombok.AllArgsConstructor;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.ToolFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InterestFactory implements ToolFactory {
	private final ApplicationContext ctx;

	@Override
	public String name() {
		return "INTEREST";
	}

	@Override
	public Tool create() {
		return ctx.getBean(InterestTool.class);
	}
}


