package me.whereareiam.yuisynapse.tools.language;

import lombok.AllArgsConstructor;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.ToolFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LanguageEnforcerFactory implements ToolFactory {
	private final ApplicationContext ctx;

	@Override
	public String name() {
		return "LANGUAGE_ENFORCER";
	}

	@Override
	public Tool create() {
		return ctx.getBean(LanguageEnforcerTool.class);
	}
}


