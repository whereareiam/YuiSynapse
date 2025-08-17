package me.whereareiam.yuisynapse.common.tool;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.tool.ToolFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ToolRegistry {
	private final Map<String, ToolFactory> factories = new HashMap<>();

	@Autowired
	public ToolRegistry(List<ToolFactory> discoveredFactories) {
		for (ToolFactory factory : discoveredFactories) {
			this.factories.put(factory.name(), factory);
			log.debug("Registered tool factory: {}", factory.name());
		}
	}

	public Tool create(String name) {
		ToolFactory factory = factories.get(name);
		if (factory == null) return null;
		return factory.create();
	}
}


