package me.whereareiam.yuisynapse.tools.user;

import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.ToolFactory;
import org.springframework.stereotype.Component;

@Component
public class UserInformationFactory implements ToolFactory {
	@Override
	public String name() {
		return "USER_INFO";
	}

	@Override
	public Tool create() {
		return new UserInformationTool();
	}
}


