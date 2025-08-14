package me.whereareiam.yuisynapse;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.output.plugin.YuiPlugin;
import me.whereareiam.yuisynapse.api.model.config.SynapseSettings;
import org.springframework.context.ApplicationContext;

@AllArgsConstructor
public class YuiSynapse implements YuiPlugin {
	private final ApplicationContext ctx;

	@Override
	public void onEnable() {
		ctx.getBean(SynapseSettings.class);
	}

	@Override
	public void onUnload() {
	}
}
