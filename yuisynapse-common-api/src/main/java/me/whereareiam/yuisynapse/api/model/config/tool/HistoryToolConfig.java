package me.whereareiam.yuisynapse.api.model.config.tool;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;

@Getter
@Setter
@NoArgsConstructor
public class HistoryToolConfig extends ToolConfig {
	private int retentionCount;
	private long ttlSeconds;
	private boolean perUser;
	private boolean perChannel;
}


