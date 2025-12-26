package me.whereareiam.yuisynapse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.plugin.YuiPlugin;
import me.whereareiam.yuisynapse.common.service.DefaultSynapseService;
import org.springframework.context.ApplicationContext;

@Slf4j
@AllArgsConstructor
public class YuiSynapse implements YuiPlugin {
    private final ApplicationContext ctx;

    @Override
    public void onDisable() {
        DefaultSynapseService service = ctx.getBean(DefaultSynapseService.class);
        service.cancelAllSessions();
        log.debug("All sessions cancelled");
    }
}
